package com.arunge.el.application;

import com.arunge.el.processing.KBEntityConverter;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class KBEntityConversion {

    private static String store = "train";
    
    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore es;
        KBEntityConverter pipeline;
        switch(store) {
        case "kb":
            es = MongoEntityStore.kbStore(client);
            es.clearEntities();
            pipeline = new KBEntityConverter(false);
            pipeline.process(es);
            break;
        case "train":
            es = MongoEntityStore.trainStore(client);
            es.clearEntities();
            pipeline = new KBEntityConverter(true);
            pipeline.process(es);
            break;
        case "eval":
            es = MongoEntityStore.evalStore(client);
            es.clearEntities();
            pipeline = new KBEntityConverter(false);
            pipeline.process(es);
            break;
        default:
            break;
        }

    }
    
}
