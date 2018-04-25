package com.arunge.el.kb.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.el.store.mongo.MongoNLPFields;
import com.mongodb.MongoClient;

public class LoadSennaNER {

    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        File nerDir = new File("J:/entity-linking-data/eval-queries-senna");
        
        for(File f : nerDir.listFiles()) {
            String entityId = f.getName().replaceAll(".txt", "");
            TextEntity e = store.fetchKBText(entityId).get();
            KBEntity kbe = store.fetchEntity(entityId).get();
            String[] nameWords = e.getName().split(" ");
//            System.out.println(entityId + ", " + e.getName());
            boolean typeFound = false;
            try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line = "";
                int matchedWord = 0;
                while((line = reader.readLine()) != null) {
                    String[] lineParts = line.trim().split("\\s+");
                    String word  = lineParts[0];
                    if(word.equals("Lexington")) { 
                        System.out.println(f.getName());
                    }
                    if(word.equals(nameWords[matchedWord])) {
                        matchedWord += 1;
                        if(matchedWord == nameWords.length) {
                            String nerTag = lineParts[1].trim();
                            EntityType type = null;
                            if(nerTag.endsWith("PER")) { 
                                type = EntityType.PERSON;
                            } else if(nerTag.endsWith("LOC")) { 
                                type = EntityType.GPE;
                            } else if(nerTag.endsWith("ORG")) {
                                type = EntityType.ORG;
                            } else {
                                type = EntityType.UNK;
                            }
                            if(type.equals(EntityType.GPE)) {
                                String next = reader.readLine();
                                String[] nextParts = next.trim().split("\\s+");
                                if(nextParts[0].equals(",")) {
                                    nextParts = reader.readLine().trim().split("\\s+");
                                    if(nextParts[0].matches("[A-Z]{2}")) {
                                        System.out.println(word + ", " + nextParts[0]);
                                    }
                                }
                            }
//                            if(kbe.getType().equals(EntityType.UNK)) {
//                                store.updateNLPDocument(kbe.getId(), MongoNLPFields.NER_TYPE, type.name());
//                            }
//                            if(kbe.getType().equals(type)) {
//                                System.out.println("Types matched: " + type);
//                            } else {
//                                System.out.println("Type mismatch. Stanford: " + kbe.getType() + ", Senna: " + type);
//                            }
                            typeFound = true;
                            break;
                        }
                    }
                }
                
            }
//            if(!typeFound) { 
//                System.out.println(entityId + " had no type.");
//            }
        }
        
        client.close();
    }
    
}
