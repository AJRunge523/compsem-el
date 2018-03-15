package com.arunge.el.application;

import com.arunge.el.processing.KBEntityProcessingPipeline;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class KBEntityConversion {

    private static String entityStoreDB = "entity_store";
    
    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore es = new MongoEntityStore(client, entityStoreDB);
        es.clearEntities();
        KBEntityProcessingPipeline pipeline = new KBEntityProcessingPipeline(es);
        pipeline.process();
    }
    
}
