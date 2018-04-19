package com.arunge.el.processing.utils;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class GoldEntityTypeChecker {

    public static void main(String[] args) { 
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore kbStore = MongoEntityStore.kbStore(client);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        CloseableIterator<TextEntity> ents = store.allKBText();
        while(ents.hasNext()) { 
            TextEntity e = ents.next();
            String goldId = e.getSingleMetadata("gold").get();
            String goldNER = e.getSingleMetadata("goldNER").get();
            if(goldId.equals("NIL")) {
                continue;
            }
//            TextEntity textEnt = kbStore.fetchKBText(goldId).get();
            KBEntity kbEnt = kbStore.fetchEntity(goldId).get();
            if(kbEnt.getType().equals(EntityType.UNK)) {
                System.out.println(goldId + " --> " + goldNER);
            }
        }
    }
    
}
