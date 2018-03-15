package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoEntityFields.CANONICAL_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_BIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_UNIGRAMS;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.TextEntity;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.iterators.CloseableIterators;
import com.arunge.unmei.iterators.Iterators;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;

/**
 * 
 *<p>Mongo-backed implementation of the {@link EntityKBStore}.<p>
 *
 * @author Andrew Runge
 *
 */
public class MongoEntityStore implements EntityKBStore {

    
    MongoCollection<Document> docs;
    MongoCollection<Document> nlpDocs;
    MongoCollection<Document> entities;
    
    public MongoEntityStore(MongoClient client, String dbName) {
        MongoDatabase db = client.getDatabase(dbName);
        this.docs = db.getCollection("docs");
        this.nlpDocs = db.getCollection("nlp_docs");
        this.entities = db.getCollection("entities");
        this.entities.createIndex(Indexes.descending(CANONICAL_NAME));
        this.entities.createIndex(Indexes.descending(KB_NAME));
        this.entities.createIndex(Indexes.descending(NAME_BIGRAMS));
        this.entities.createIndex(Indexes.descending(NAME_UNIGRAMS));

    }
    
    @Override
    public String insert(TextEntity doc) {
        Document d = docs.find(eq("_id", doc.getId())).first();
        if(d != null) {
            return doc.getId();
        }
        Document mongoDoc = MongoKBDocumentConverter.toMongoDocument(doc);
        docs.insertOne(mongoDoc);
        return doc.getId();
    }
    
    @Override
    public Optional<TextEntity> fetchKBText(String id) {
        Document d = docs.find(eq("_id", id)).first();
        if(d == null) {
            return Optional.empty();
        }
        return Optional.of(MongoKBDocumentConverter.toTextEntity(d));
    }
    
    @Override
    public CloseableIterator<TextEntity> allKBText() {
        return CloseableIterators.wrap(Iterators.map(docs.find().noCursorTimeout(true).iterator(), MongoKBDocumentConverter::toTextEntity));
    }    
    
    @Override
    public String insert(KBEntity entity) {
        Document d = entities.find(eq("_id", entity.getId())).first();
        if(d != null) {
            return entity.getId();
        }
        Document mongoDoc = MongoEntityConverter.toMongoDocument(entity);
        entities.insertOne(mongoDoc);
        return entity.getId();
    }

    @Override
    public Optional<KBEntity> fetchEntity(String id) {
        Document d = entities.find(eq("_id", id)).first();
        if(d == null) {
            return Optional.empty();
        } 
        return Optional.of(MongoEntityConverter.toEntity(d));
    }
    
    @Override
    public CloseableIterator<KBEntity> allEntities() {
        return CloseableIterators.wrap(Iterators.map(entities.find().noCursorTimeout(true).iterator(), MongoEntityConverter::toEntity));
    }
    
    @Override
    public CloseableIterator<KBEntity> query(EntityQuery query) {
        List<Bson> nameFilters = new ArrayList<>();
        for(String name : query.getNameVariants()) {
            nameFilters.add(eq(CANONICAL_NAME, name));
        }
        List<Bson> tokenFilters = new ArrayList<>();
        for(String token : query.getNameUnigrams()) {
            tokenFilters.add(eq(NAME_UNIGRAMS, token));
        }
        List<Bson> filters = new ArrayList<>();
        if(tokenFilters.size() > 0) {
            filters.add(or(tokenFilters));
        }
        if(nameFilters.size() > 0) {
            filters.add(or(nameFilters));
        }
        if(filters.size() > 0) { 
            Bson mongoQuery = or(or(nameFilters), or(tokenFilters));
            return CloseableIterators.wrap(Iterators.map(entities.find(mongoQuery).noCursorTimeout(true).iterator(), MongoEntityConverter::toEntity));
        }
        return CloseableIterators.wrap(new ArrayList<KBEntity>(0).iterator());
    }

    @Override
    public void clearEntities() { 
        entities.drop();
    }
    
}
