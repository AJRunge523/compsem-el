package com.arunge.el.processing.utils;

import java.util.stream.Stream;

import com.arunge.el.api.KBEntity;
import com.arunge.el.nlp.dist.ContextStore;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;

public class EntityRetrievalTester {

    public static void main(String[] args) { 
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.kbStore(client);
        KBEntity e = new KBEntity("test");
        e.setName("Matthaeus");
        e.setCleansedName("Matthaeus");
        e.setCleansedAliases(new String[] {"lothar matthaeus"});
        EntityCandidateRetrievalEngine engine = new EntityCandidateRetrievalEngine(store, ContextStore.getDefault());
        Stream<KBEntity> entities = engine.retrieveCandidates(e);
        entities.forEach(ent -> System.out.println(ent.getId() + ", " + ent.getName()));
        
    }
    
}
