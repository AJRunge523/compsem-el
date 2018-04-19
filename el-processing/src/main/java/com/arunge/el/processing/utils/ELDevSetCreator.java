package com.arunge.el.processing.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class ELDevSetCreator {

    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.trainStore(client);
        CloseableIterator<TextEntity> entities = store.allKBText();
        Map<String, List<String>> entityMap = new HashMap<>();
        while(entities.hasNext()) {
            TextEntity e = entities.next();
            String ner = e.getSingleMetadata("goldNER").get();
            String label = e.getSingleMetadata("gold").get();
            String key = ner + "." + (label.equals("NIL") ? label : "ENT");
            if(!entityMap.containsKey(key)) {
                entityMap.put(key, new ArrayList<>());
            }
            entityMap.get(key).add(e.getId());
        }
        Set<String> ids = new HashSet<>();
        for(String key : entityMap.keySet()) {
            System.out.println(key);
            List<String> ents = entityMap.get(key);
            int numToDraw = 30;
            if(key.contains("NIL")) {
                numToDraw = 20;
            }
            while(ids.size() < numToDraw) {
                int randIndex = (int)(Math.random() * ents.size());
                ids.add(ents.get(randIndex));
            }
            for(String id : ids) {
                System.out.println(id);
            }
            ids.clear();
        }
        client.close();
    }
    
}
