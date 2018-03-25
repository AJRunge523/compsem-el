package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoEntityFields.ACRONYM;
import static com.arunge.el.store.mongo.MongoEntityFields.CANONICAL_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_BIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_UNIGRAMS;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
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

    private static Logger LOG = LoggerFactory.getLogger(MongoEntityStore.class);
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
        this.entities.createIndex(Indexes.descending(ACRONYM));

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
    public String insert(NLPDocument doc) {
        Document d = nlpDocs.find(eq("_id", doc.getId())).first();
        if(d != null) {
            return doc.getId();
        }
        Document mongoDoc = MongoNLPDocumentConverter.toMongoDocument(doc);
        nlpDocs.insertOne(mongoDoc);
        return doc.getId();
    }
    
    @Override
    public Optional<NLPDocument> fetchNLPDocument(String id) {
        Document d = nlpDocs.find(eq("_id", id)).first();
        if(d == null) {
            return Optional.empty();
        }
        return Optional.of(MongoNLPDocumentConverter.toNLPDocument(d));
    }
    
    @Override
    public CloseableIterator<NLPDocument> allNLPDocuments() {
        return CloseableIterators.wrap(Iterators.map(nlpDocs.find().noCursorTimeout(true).iterator(), MongoNLPDocumentConverter::toNLPDocument));
    }    
    
    @Override
    public void updateNLPDocument(String id, String field, Object value) {
        Document update = new Document();
        if(value instanceof Map) {
            Document valueDoc = new Document();
            Map<?, ?> valueMap = (Map<?, ?>) value;
            for(Object key : valueMap.keySet()) {
                valueDoc.append(key.toString(), valueMap.get(key));
            }
            update.append(field, valueDoc);
        } else if(value instanceof String) {
            update.append(field, value);
        } else if(value instanceof Set) {
            update.append(field, value);
        }
        nlpDocs.updateOne(eq("_id", id), new Document("$set", update));
    }

    @Override
    public void clearNLPDocument(String field) {
        nlpDocs.updateMany(new Document(), new Document("$unset", new Document(field, "")));
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
        
        List<Bson> filters = new ArrayList<>();
        for(String name : query.getNameVariants()) {
            filters.add(eq(CANONICAL_NAME, name));
            String[] words = name.split("\\s+");
            List<Bson> wordFilters = new ArrayList<>();
            for(String word : words) {
                wordFilters.add(eq(NAME_UNIGRAMS, word));
            }
            filters.add(and(wordFilters));
        }
        
        for(String acronym : query.getAcronyms()) {
            filters.add(eq(ACRONYM, acronym));
        }
        
        if(filters.size() > 0) { 
            Bson mongoQuery = or(filters);
            return CloseableIterators.wrap(Iterators.map(entities.find(mongoQuery).noCursorTimeout(true).iterator(), MongoEntityConverter::toEntity));
        }
        return CloseableIterators.wrap(new ArrayList<KBEntity>(0).iterator());
    }

    @Override
    public void clearEntities() { 
        entities.drop();
        this.entities.createIndex(Indexes.descending(CANONICAL_NAME));
        this.entities.createIndex(Indexes.descending(KB_NAME));
        this.entities.createIndex(Indexes.descending(NAME_BIGRAMS));
        this.entities.createIndex(Indexes.descending(NAME_UNIGRAMS));
    }


    
}
