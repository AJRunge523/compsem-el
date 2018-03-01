package com.arunge.el.mongo.ingest;

import static com.mongodb.client.model.Filters.eq;

import java.util.Iterator;
import java.util.Optional;

import org.bson.Document;

import com.arunge.el.api.KBDocument;
import com.arunge.unmei.iterators.Iterators;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

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
    
    public Iterator<KBDocument> all() {
        return Iterators.map(coll.find().noCursorTimeout(true).iterator(), d -> MongoKBDocumentConverter.toKBDocument(d));
    }
    
}
