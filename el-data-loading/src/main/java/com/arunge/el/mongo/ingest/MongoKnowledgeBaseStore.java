package com.arunge.el.mongo.ingest;

import org.bson.Document;

import com.arunge.el.api.KBDocument;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.or;

import java.util.Optional;

public class MongoKnowledgeBaseStore {

    private MongoCollection<Document> coll;
    
    public MongoKnowledgeBaseStore(MongoClient client, String dbName) {
        this.coll = client.getDatabase(dbName).getCollection("docs");
    }
    
    public String insert(KBDocument doc) {
        Document d = coll.find(eq("_id", doc.getId())).first();
        if(d != null) {
            return doc.getId();
        }
        Document mongoDoc = MongoKBDocumentConverter.toMongoDocument(doc);
        coll.insertOne(mongoDoc);
        return doc.getId();
    }
    
    public Optional<KBDocument> fetch(String id) {
        Document d = coll.find(eq("_id", id)).first();
        if(d == null) {
            return Optional.empty();
        }
        return Optional.of(MongoKBDocumentConverter.toKBDocument(d));
    }
    
}
