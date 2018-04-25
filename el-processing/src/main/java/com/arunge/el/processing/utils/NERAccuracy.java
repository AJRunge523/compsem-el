package com.arunge.el.processing.utils;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class NERAccuracy {

    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        CloseableIterator<KBEntity> iter = store.allEntities();
        double positives = 0;
        double total = 0;
        int[][] confMatrix = new int[4][4];
        while(iter.hasNext()) {
            KBEntity e = iter.next();
            String goldNER = e.getAttribute(EntityAttribute.GOLD_NER).getValueAsStr();
            EntityType gold = null;
            if(goldNER.equals("PER")) {
                gold = EntityType.PERSON;
            } else {
                gold = EntityType.valueOf(goldNER);
            }
            EntityType ner = e.getType();
            
            confMatrix[gold.ordinal()][ner.ordinal()] += 1;
            
            if(goldNER.equals("PER") && ner.name().equals("PERSON")) {
                positives += 1;
            } else if(goldNER.equals("GPE") && ner.name().equals("GPE")) { 
                positives += 1;
            } else if(goldNER.equals("ORG") && (ner.name().equals("ORG") || ner.name().equals("UNK"))) {
                positives += 1;
            } else {
                System.out.println(e.getId());
            }
            total += 1;
        }
        System.out.println("Gold\tPERSON\tGPE\tORG\tUNK\t\tLabeled");
        for(int i = 0; i < 4; i++) {
            System.out.print(EntityType.values()[i].name() + "\t");
            
            for(int j = 0; j < 4; j++) {
                System.out.print(confMatrix[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println("NER Accuracy: " + (positives / total));
    }
    
}
