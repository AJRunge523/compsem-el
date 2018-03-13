package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoEntityFields.CANONICAL_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_BIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_UNIGRAMS;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.EntityStore;
import com.arunge.el.api.KBEntity;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.iterators.CloseableIterators;
import com.arunge.unmei.iterators.Iterators;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

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
            return CloseableIterators.wrap(Iterators.map(coll.find(mongoQuery).noCursorTimeout(true).iterator(), MongoEntityConverter::toEntity));
        }
        return CloseableIterators.wrap(new ArrayList<KBEntity>(0).iterator());
    }

}
