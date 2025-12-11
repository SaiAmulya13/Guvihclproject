package com.inventory.dao;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoManager {
    private static MongoClient client;
    private static MongoDatabase database;

    // Change this connection string if needed
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "hardware_inventory_db";

    public static void init() {
        if (client == null) {
            ConnectionString conn = new ConnectionString(CONNECTION_STRING);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(conn)
                    .build();
            client = MongoClients.create(settings);
            database = client.getDatabase(DB_NAME);
        }
    }

    public static MongoDatabase getDatabase() {
        if (database == null) init();
        return database;
    }

    public static void close() {
        if (client != null) client.close();
    }
}
