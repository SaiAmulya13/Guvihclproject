package com.inventory.model;

import org.bson.types.ObjectId;

public class HardwareItem {
    private String id; // store as string of ObjectId
    private String name;
    private String serialNumber;
    private String location;
    private String status;

    public HardwareItem() {}

    public HardwareItem(String name, String serialNumber, String location, String status) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.location = location;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getSerialNumber() { return serialNumber; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "HardwareItem{id=" + id + ", name=" + name + ", serial=" + serialNumber +
                ", location=" + location + ", status=" + status + "}";
    }
}
