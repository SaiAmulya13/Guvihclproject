package com.inventory.dao;

import com.inventory.model.Dependency;
import com.inventory.model.Project;
import com.inventory.model.VulnerabilityReport;
import com.inventory.model.VulnerabilityReport.Finding;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

/**
 * ProjectDAO handles CRUD for projects and vulnerability checks / recommendations.
 *
 * Vulnerability logic (simple and deterministic):
 * - vulnerabilities are stored in collection "vulnerabilities"
 *   Each vulnerability doc fields:
 *     groupId, artifactId, vulnerableVersion, cve, severity, patchedVersion, recommendation
 *
 * - recommendations may be stored in "dependency_recommendations" (optional)
 */
public class ProjectDAO {
    private final MongoCollection<Document> col;
    private final MongoCollection<Document> vulnCol;
    private final MongoCollection<Document> recCol;

    public ProjectDAO() {
        col = MongoManager.getDatabase().getCollection("projects");
        vulnCol = MongoManager.getDatabase().getCollection("vulnerabilities");
        recCol = MongoManager.getDatabase().getCollection("dependency_recommendations");
    }

    public String create(Project p) {
        Document doc = new Document()
                .append("name", p.getName())
                .append("description", p.getDescription());

        List<Document> deps = new ArrayList<>();
        for (Dependency d : p.getDependencies()) {
            deps.add(new Document("groupId", d.getGroupId())
                    .append("artifactId", d.getArtifactId())
                    .append("version", d.getVersion()));
        }
        doc.append("dependencies", deps);

        col.insertOne(doc);
        ObjectId id = doc.getObjectId("_id");
        return id.toHexString();
    }

    public Project read(String id) {
        Document doc = col.find(Filters.eq("_id", new ObjectId(id))).first();
        if (doc == null) return null;

        Project p = new Project();
        p.setId(doc.getObjectId("_id").toHexString());
        p.setName(doc.getString("name"));
        p.setDescription(doc.getString("description"));

        Object depsObj = doc.get("dependencies");
        if (depsObj instanceof List<?>) {
            List<?> rawList = (List<?>) depsObj;
            List<Dependency> list = new ArrayList<>();
            for (Object o : rawList) {
                if (o instanceof Document d) {
                    list.add(new Dependency(
                            d.getString("groupId"),
                            d.getString("artifactId"),
                            d.getString("version")
                    ));
                }
            }
            p.setDependencies(list);
        }

        return p;
    }

    public List<Project> listAll() {
        List<Project> out = new ArrayList<>();
        FindIterable<Document> it = col.find();

        for (Document doc : it) {
            Project p = new Project();
            p.setId(doc.getObjectId("_id").toHexString());
            p.setName(doc.getString("name"));
            p.setDescription(doc.getString("description"));

            Object depsObj = doc.get("dependencies");
            if (depsObj instanceof List<?>) {
                List<?> rawList = (List<?>) depsObj;
                List<Dependency> depsList = new ArrayList<>();
                for (Object o : rawList) {
                    if (o instanceof Document d) {
                        depsList.add(new Dependency(
                                d.getString("groupId"),
                                d.getString("artifactId"),
                                d.getString("version")
                        ));
                    }
                }
                p.setDependencies(depsList);
            }

            out.add(p);
        }

        return out;
    }

    public boolean addDependency(String projectId, Dependency d) {
        Document doc = new Document("groupId", d.getGroupId())
                .append("artifactId", d.getArtifactId())
                .append("version", d.getVersion());
        Document push = new Document("$push", new Document("dependencies", doc));
        return col.updateOne(Filters.eq("_id", new ObjectId(projectId)), push).getModifiedCount() > 0;
    }

    public boolean updateDependency(String projectId, Dependency oldDep, Dependency newDep) {
        Document filter = new Document("_id", new ObjectId(projectId))
                .append("dependencies", new Document("$elemMatch",
                        new Document("groupId", oldDep.getGroupId())
                                .append("artifactId", oldDep.getArtifactId())
                                .append("version", oldDep.getVersion())));
        Document newDoc = new Document("dependencies.$.groupId", newDep.getGroupId())
                .append("dependencies.$.artifactId", newDep.getArtifactId())
                .append("dependencies.$.version", newDep.getVersion());
        Document update = new Document("$set", newDoc);
        return col.updateOne(filter, update).getModifiedCount() > 0;
    }

    public boolean deleteProject(String projectId) {
        return col.deleteOne(Filters.eq("_id", new ObjectId(projectId))).getDeletedCount() > 0;
    }

    public VulnerabilityReport checkVulnerabilities(String projectId) {
        Project p = read(projectId);
        VulnerabilityReport report = new VulnerabilityReport();
        if (p == null) return report;

        for (Dependency dep : p.getDependencies()) {
            Document vulnFilter = new Document("groupId", dep.getGroupId())
                    .append("artifactId", dep.getArtifactId())
                    .append("vulnerableVersion", dep.getVersion());

            Document found = vulnCol.find(vulnFilter).first();
            if (found != null) {
                String cve = found.getString("cve");
                String severity = found.getString("severity");
                String patched = found.getString("patchedVersion");
                String recommendation = found.getString("recommendation");

                if (recommendation == null) {
                    recommendation = "Upgrade to " + (patched != null ? patched : "latest safe version");
                }

                report.addFinding(new Finding(dep, cve, severity, patched, recommendation));
            }
        }

        return report;
    }

    public List<String> recommendDependencyUpdates(String projectId) {
        Project p = read(projectId);
        List<String> recs = new ArrayList<>();
        if (p == null) return recs;

        for (Dependency dep : p.getDependencies()) {
            Document vulnFilter = new Document("groupId", dep.getGroupId())
                    .append("artifactId", dep.getArtifactId())
                    .append("vulnerableVersion", dep.getVersion());
            Document foundVuln = vulnCol.find(vulnFilter).first();

            if (foundVuln != null) {
                String patched = foundVuln.getString("patchedVersion");
                String recommendation = foundVuln.getString("recommendation");
                if (recommendation == null)
                    recommendation = "Upgrade to " + (patched != null ? patched : "latest patched version");
                recs.add(dep.toString() + " => VULNERABLE -> " + recommendation);
                continue;
            }

            Document recFilter = new Document("groupId", dep.getGroupId())
                    .append("artifactId", dep.getArtifactId());
            Document recDoc = recCol.find(recFilter).first();

            if (recDoc != null) {
                String suggested = recDoc.getString("suggestedVersion");
                String reason = recDoc.getString("reason");
                if (suggested != null && !suggested.equals(dep.getVersion())) {
                    recs.add(dep.toString() + " => Suggest upgrade to " + suggested +
                            " (" + (reason != null ? reason : "recommended") + ")");
                }
            }
        }

        if (recs.isEmpty()) recs.add("No updates or vulnerabilities found for project dependencies.");
        return recs;
    }
}
