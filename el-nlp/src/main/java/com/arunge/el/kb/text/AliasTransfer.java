package com.arunge.el.kb.text;

import com.arunge.el.api.KBEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.el.store.mongo.MongoNLPFields;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class AliasTransfer {

    public static void main(String[] args) { 
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.kbStore(client);
        CloseableIterator<KBEntity> entities = store.allEntities();
        int numProcessed = 0;
        while(entities.hasNext()) {
            KBEntity e = entities.next();
            store.updateNLPDocument(e.getId(), MongoNLPFields.EXPANDED_ALIASES, e.getAliases().get());
            store.updateNLPDocument(e.getId(), MongoNLPFields.CLEANSED_ALIASES, e.getCleansedAliases().get());
            numProcessed += 1;
            if(numProcessed % 10000 == 0) { 
                System.out.println("Finished " + numProcessed + " entities.");
            }
        }
    }
    
}
