package com.inventory;

import com.inventory.dao.MongoManager;
import com.inventory.dao.HardwareDAO;
import com.inventory.dao.ProjectDAO;
import com.inventory.model.HardwareItem;
import com.inventory.model.Project;
import com.inventory.model.Dependency;
import com.inventory.model.VulnerabilityReport;
import com.inventory.util.Utils;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Starting Hardware Inventory Management (MongoDB driver 4.11.0)");
        MongoManager.init();

        HardwareDAO hardwareDAO = new HardwareDAO();
        ProjectDAO projectDAO = new ProjectDAO();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": createHardware(hardwareDAO); break;
                    case "2": listHardware(hardwareDAO); break;
                    case "3": createProject(projectDAO); break;
                    case "4": listProjects(projectDAO); break;
                    case "5": addDependencyToProject(projectDAO); break;
                    case "6": checkVulnerabilities(projectDAO); break;
                    case "7": recommendUpdates(projectDAO); break;
                    case "0": running = false; break;
                    default: System.out.println("Unknown option");
                }
            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        }

        System.out.println("Exiting...");
        MongoManager.close();
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1) Create hardware item");
        System.out.println("2) List hardware items");
        System.out.println("3) Create project");
        System.out.println("4) List projects");
        System.out.println("5) Add dependency to project");
        System.out.println("6) Check vulnerabilities (check_vulnerabilities(projectId))");
        System.out.println("7) Recommend dependency updates (recommend_dependency_updates(projectId))");
        System.out.println("0) Exit");
        System.out.print("Choose: ");
    }

    private static void createHardware(HardwareDAO dao) {
        System.out.println("Create hardware:");
        String name = Utils.readLine("Name: ");
        String serial = Utils.readLine("Serial number: ");
        String location = Utils.readLine("Location: ");
        String status = Utils.readLine("Status: ");
        HardwareItem item = new HardwareItem(name, serial, location, status);
        String id = dao.create(item);
        System.out.println("Hardware created with id: " + id);
    }

    private static void listHardware(HardwareDAO dao) {
        System.out.println("All hardware items:");
        List<com.inventory.model.HardwareItem> items = dao.listAll();
        for (com.inventory.model.HardwareItem it : items) {
            System.out.println(" - " + it.toString());
        }
    }

    private static void createProject(ProjectDAO dao) {
        System.out.println("Create project:");
        String name = Utils.readLine("Name: ");
        String desc = Utils.readLine("Description: ");
        Project p = new Project(name, desc);
        boolean addDeps = Utils.readLine("Add dependencies now? (y/N): ").equalsIgnoreCase("y");
        if (addDeps) {
            while (true) {
                System.out.println("Enter dependency:");
                Dependency d = Utils.readDependencyInteractive();
                p.addDependency(d);
                String more = Utils.readLine("Add another dependency? (y/N): ");
                if (!more.equalsIgnoreCase("y")) break;
            }
        }
        String id = dao.create(p);
        System.out.println("Project created with id: " + id);
    }

    private static void listProjects(ProjectDAO dao) {
        System.out.println("Projects:");
        List<Project> list = dao.listAll();
        for (Project p : list) {
            System.out.println(" - " + p.toString());
        }
    }

    private static void addDependencyToProject(ProjectDAO dao) {
        String pid = Utils.readLine("Project Id: ");
        System.out.println("Enter dependency to add:");
        Dependency d = Utils.readDependencyInteractive();
        boolean ok = dao.addDependency(pid, d);
        System.out.println("Dependency added? " + ok);
    }

    private static void checkVulnerabilities(ProjectDAO dao) {
        String pid = Utils.readLine("Project Id: ");
        VulnerabilityReport report = dao.checkVulnerabilities(pid);
        System.out.println(report.toString());
    }

    private static void recommendUpdates(ProjectDAO dao) {
        String pid = Utils.readLine("Project Id: ");
        List<String> recs = dao.recommendDependencyUpdates(pid);
        System.out.println("Recommendations:");
        for (String r : recs) {
            System.out.println(" - " + r);
        }
    }
}
