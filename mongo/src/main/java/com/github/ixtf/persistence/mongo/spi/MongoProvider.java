package com.github.ixtf.persistence.mongo.spi;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;

/**
 * @author jzb 2019-02-14
 */
public interface MongoProvider {

    MongoClient client();

    default String dbName() {
        return "test-db";
    }

    default MongoDatabase database() {
        return client().getDatabase(dbName());
    }

    default MongoCollection<Document> collection(String name) {
        return database().getCollection(name);
    }
}
