package com.arunge.el.store.mongo;

import java.util.Optional;

import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.EntityStore;
import com.arunge.el.api.KBEntity;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.iterators.CloseableIterators;
import com.arunge.unmei.iterators.Iterators;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

import static com.arunge.el.store.mongo.MongoEntityFields.CANONICAL_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_UNIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_BIGRAMS;

/**
 * 
 *<p>Mongo-backed implementation of the {@link EntityStore}.<p>
 *
 * @author Andrew Runge
 *
 */
public class MongoEntityStore implements EntityStore {

    MongoCollection<Document> coll;
    
    public MongoEntityStore(MongoClient client, String dbName) { 
        this.coll = client.getDatabase(dbName).getCollection("entities");
        this.coll.createIndex(Indexes.descending(CANONICAL_NAME));
        this.coll.createIndex(Indexes.descending(KB_NAME));
        this.coll.createIndex(Indexes.descending(NAME_BIGRAMS));
        this.coll.createIndex(Indexes.descending(NAME_UNIGRAMS));

    }
    
    @Override
    public String insert(KBEntity entity) {
        Document d = coll.find(eq("_id", entity.getId())).first();
        if(d != null) {
            return entity.getId();
        }
        Document mongoDoc = MongoEntityConverter.toMongoDocument(entity);
        coll.insertOne(mongoDoc);
        return entity.getId();
    }

    @Override
    public Optional<KBEntity> fetch(String id) {
        Document d = coll.find(eq("_id", id)).first();
        if(d == null) {
            return Optional.empty();
        } 
        return Optional.of(MongoEntityConverter.toEntity(d));
    }

    @Override
    public CloseableIterator<KBEntity> all() {
        return CloseableIterators.wrap(Iterators.map(coll.find().noCursorTimeout(true).iterator(), MongoEntityConverter::toEntity));
    }

    @Override
    public CloseableIterator<KBEntity> query(EntityQuery query) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

}
