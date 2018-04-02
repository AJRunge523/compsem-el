package com.arunge.el.application;

import com.arunge.el.processing.KBEntityConverter;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class KBEntityConversion {

    private static String entityStoreDB = "el_training_query_store";
    
    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore es = new MongoEntityStore(client, entityStoreDB);
        es.clearEntities();
        KBEntityConverter pipeline = new KBEntityConverter();
        pipeline.process(es);
    }
    
}
