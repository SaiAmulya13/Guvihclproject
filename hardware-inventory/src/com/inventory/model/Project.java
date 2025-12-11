package com.inventory.model;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String id;
    private String name;
    private String description;
    private List<Dependency> dependencies = new ArrayList<>();

    public Project() {}

    public Project(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // getters & setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Dependency> getDependencies() { return dependencies; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setDependencies(List<Dependency> dependencies) { this.dependencies = dependencies; }

    public void addDependency(Dependency d) { this.dependencies.add(d); }

    @Override
    public String toString() {
        return "Project{id=" + id + ", name=" + name + ", desc=" + description +
                ", deps=" + dependencies + "}";
    }
}
