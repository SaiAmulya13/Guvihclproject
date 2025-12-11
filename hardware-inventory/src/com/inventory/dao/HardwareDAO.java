package com.inventory.dao;

import com.inventory.model.HardwareItem;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;

public class HardwareDAO {
    private final MongoCollection<Document> col;

    public HardwareDAO() {
        col = MongoManager.getDatabase().getCollection("hardware_items");
    }

    public String create(HardwareItem item) {
        Document doc = new Document()
                .append("name", item.getName())
                .append("serialNumber", item.getSerialNumber())
                .append("location", item.getLocation())
                .append("status", item.getStatus());
        col.insertOne(doc);
        ObjectId id = doc.getObjectId("_id");
        return id.toHexString();
    }

    public HardwareItem read(String id) {
        Document doc = col.find(Filters.eq("_id", new ObjectId(id))).first();
        if (doc == null) return null;
        HardwareItem item = new HardwareItem();
        item.setId(doc.getObjectId("_id").toHexString());
        item.setName(doc.getString("name"));
        item.setSerialNumber(doc.getString("serialNumber"));
        item.setLocation(doc.getString("location"));
        item.setStatus(doc.getString("status"));
        return item;
    }

    public List<HardwareItem> listAll() {
        List<HardwareItem> res = new ArrayList<>();
        FindIterable<Document> it = col.find();
        for (Document doc : it) {
            HardwareItem item = new HardwareItem();
            item.setId(doc.getObjectId("_id").toHexString());
            item.setName(doc.getString("name"));
            item.setSerialNumber(doc.getString("serialNumber"));
            item.setLocation(doc.getString("location"));
            item.setStatus(doc.getString("status"));
            res.add(item);
        }
        return res;
    }

    public boolean update(String id, HardwareItem updated) {
        Document update = new Document()
                .append("name", updated.getName())
                .append("serialNumber", updated.getSerialNumber())
                .append("location", updated.getLocation())
                .append("status", updated.getStatus());
        Document set = new Document("$set", update);
        return col.updateOne(Filters.eq("_id", new ObjectId(id)), set).getModifiedCount() > 0;
    }

    public boolean delete(String id) {
        return col.deleteOne(Filters.eq("_id", new ObjectId(id))).getDeletedCount() > 0;
    }
}
