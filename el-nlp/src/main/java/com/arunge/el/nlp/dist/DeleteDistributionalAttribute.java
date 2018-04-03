package com.arunge.el.nlp.dist;

import com.arunge.el.api.ContextType;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class DeleteDistributionalAttribute {

    public static void main(String[] args) {
        String attributeToDelete = ContextType.TOPIC_200.name();
        MongoEntityStore store = new MongoEntityStore(new MongoClient("localhost", 27017), "entity_store");
        store.clearNLPDocument(attributeToDelete);
    }
    
}
