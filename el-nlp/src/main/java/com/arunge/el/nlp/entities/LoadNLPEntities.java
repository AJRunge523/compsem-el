package com.arunge.el.nlp.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.el.store.mongo.MongoNLPFields;
import com.mongodb.MongoClient;

public class LoadNLPEntities {

    public static void main(String[] args) throws IOException { 
        
        File entityFile = new File("output/eval-entities-v2.txt");
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        try(BufferedReader reader = new BufferedReader(new FileReader(entityFile))) { 
            String id = reader.readLine();
            while(id != null) {
                id = loadEntry(reader, store, id);
            }
        }
    }
    
    private static String loadEntry(BufferedReader reader, MongoEntityStore store, String id) throws IOException {
        TextEntity te = store.fetchKBText(id).get();
        String name = te.getName();
        boolean isAcronym = name.matches("[A-Z]{2,}");
        String line = "";
        Set<String> corefEntities = new HashSet<>();
        Set<String> aliases = new HashSet<>();
        EntityType type = EntityType.UNK;
        List<String[]> entryLines = new ArrayList<>();
        String[] nameLine = null;
        
        //Try to find the line containing the entity mention - prefer exact match
        while((line = reader.readLine()) != null && line.startsWith("\t")) {
            String[] names = line.split("\t");
            for(int i = 1; i < names.length; i+=2) {
                String n = names[i];
                if(n.equals(name)) {
//                    System.out.println("Exact match");
                    nameLine = names;
                    break;
                }
            }
            
            entryLines.add(names);
        }
        //If haven't found the line, look for the shortest name that fully contains the query mention text
        String nameLower = name.toLowerCase();
        if(nameLine == null) {
//            String currCand = "";
            int candLength = Integer.MAX_VALUE;
            for(String[] entry : entryLines ) {
                for(int i = 1; i < entry.length; i+=2) {
                    if(entry[i].toLowerCase().contains(nameLower) && entry[i].length() < candLength) {
//                        currCand = entry[i];
                        nameLine = entry;
                        candLength = entry[i].length();
                    } else if(isAcronym && name.equals(getAcronym(entry[i]))) {
                        System.out.println("Found acronym match for " + id + " with name " + name + ": " + entry[1]);
                        nameLine = entry;
                        candLength = entry[i].length();
                    }
                }
            }
        }
        //Pick the entity type based on highest type vote of all aliases
        Map<String, Integer> typeCounts = new HashMap<>();
        if(nameLine != null) {
            for(int i = 1; i < nameLine.length; i+=2) {
                if(!nameLine[i].toLowerCase().equals(nameLower)) {
                    aliases.add(nameLine[i]);
                }
                if(!typeCounts.containsKey(nameLine[i+1])) {
                    typeCounts.put(nameLine[i+1], 0);
                }
                typeCounts.put(nameLine[i+1], typeCounts.get(nameLine[i+1])+1);
            }
            int count = 0;
            if(typeCounts.containsKey("PERSON") && typeCounts.get("PERSON") > count) {
                type = EntityType.PERSON;
                count = typeCounts.get("PERSON");
            }
            if(typeCounts.containsKey("LOCATION") && typeCounts.get("LOCATION") > count) {
                type = EntityType.GPE;
                count = typeCounts.get("LOCATION");
            }
            if(typeCounts.containsKey("ORGANIZATION") && typeCounts.get("ORGANIZATION") > count) {
                type = EntityType.ORG;
                count = typeCounts.get("ORGANIZATION");
            }
        } else {
            System.out.println("Unable to find name for query entity " + id + " with name " +  name);
        }
        // Add all non-alias lines as coref entities
        for(String[] entry : entryLines) {
            if(!entry.equals(nameLine)) { 
                for(int i = 1; i < entry.length; i+=2) {
                    //Don't add names that were also aliases as coref entities to be safe
                    if(entry[i].length() < 50 && !aliases.contains(entry[i])) {
                        corefEntities.add(entry[i]);
                    }
                }
            }
        }
        
//        store.updateNLPDocument(id, MongoNLPFields.NER_TYPE, type.name());
        store.updateNLPDocument(id, MongoNLPFields.COREF_ENTITIES, corefEntities);
        store.updateNLPDocument(id, MongoNLPFields.WIKI_ALIASES, aliases);
        
        
//        System.out.println("Entity " + id + " with name " + name + " has type " + type);
        return line;
    }
    
    private static String getAcronym(String name) {
        String acr = "";
        String[] parts = name.split(" ");
        for(String p : parts) {
            if(p.charAt(0) >= 'A' && p.charAt(0) <= 'Z') {
                acr += p.charAt(0);
            }
        }
        return acr;
    }
    
}
