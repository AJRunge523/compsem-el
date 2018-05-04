package com.arunge.el.nlp.dist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.arunge.el.api.ContextType;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class InfoboxDistLoader {

    public static void main(String[] args) throws IOException { 
        ContextType type = ContextType.ENT_IB_DIST;
        File vecFile = new File("output/entity-vectors/v3/infobox-name-vectors.txt");
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.kbStore(client);
        try(BufferedReader reader = new BufferedReader(new FileReader(vecFile))) {
            String line = "";
            while((line = reader.readLine()) != null) { 
                String[] parts = line.split("\t");
                String id = parts[0];
                HashMap<Integer, Double> infoboxDist = new HashMap<>();
                for(int i = 1; i < parts.length; i++) {
                    String[] indexVal = parts[i].split(":");
                    infoboxDist.put(Integer.parseInt(indexVal[0]), Double.parseDouble(indexVal[1]));
                }
                store.updateNLPDocument(id, type.name(), infoboxDist);
            }
        }
        client.close();
    }
    
}
