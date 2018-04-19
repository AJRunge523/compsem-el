package com.arunge.el.nlp.utils;

import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.el.store.mongo.MongoNLPFields;
import com.mongodb.MongoClient;

public class DeleteNLPAttribute {

    public static void main(String[] args) {
        String attributeToDelete = MongoNLPFields.CLEANSED_ALIASES;
        MongoEntityStore store = new MongoEntityStore(new MongoClient("localhost", 27017), "entity_store");
        store.clearNLPDocument(attributeToDelete);
    }
    
}
