package com.arunge.el.processing;

import com.arunge.el.mongo.ingest.MongoKnowledgeBaseStore;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class KBEntityConversion {

    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoKnowledgeBaseStore kb = new MongoKnowledgeBaseStore(client, "tackbp");
        MongoEntityStore es = new MongoEntityStore(client, "entity_store");
        KBEntityProcessingPipeline pipeline = new KBEntityProcessingPipeline(kb, es);
        pipeline.process();
    }
    
}
