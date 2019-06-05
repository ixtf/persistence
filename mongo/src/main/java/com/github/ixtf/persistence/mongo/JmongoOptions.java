package com.github.ixtf.persistence.mongo;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;

/**
 * @author jzb 2019-02-18
 */
public abstract class JmongoOptions {

    public abstract MongoClient client();

    public String dbName() {
        return "test-db";
    }

    public MongoDatabase database() {
        return client().getDatabase(dbName());
    }

    public MongoCollection<Document> collection(String name) {
        return database().getCollection(name);
    }
}
