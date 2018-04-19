package com.arunge.el.processing.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GoldInfoSupplier {

    private Map<String, String> goldNER;
    private Map<String, String> goldEntities;
    
    private GoldInfoSupplier(File goldFile) {
        goldNER = new HashMap<>();
        goldEntities = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(goldFile))) {
            String line = "";
            while((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] parts = line.split("\\s+");
                String id = parts[0];
                String gold = parts[1];
                if(gold.startsWith("NIL")) { 
                    gold = "NIL";
                }
                String goldNERTag = parts[2];
                goldNER.put(id, goldNERTag);
                goldEntities.put(id, gold);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to load gold file", e);
        }
    }
    
    public String getGoldNER(String id) { 
        return goldNER.get(id);
    }
    
    public String getGoldEntity(String id) {
        return goldEntities.get(id);
    }
    
    public static GoldInfoSupplier goldTrain() { 
        return new GoldInfoSupplier(new File("src/main/resources/train-gold.txt"));
    }
    
    public static GoldInfoSupplier goldEval() { 
        return new GoldInfoSupplier(new File("src/main/resources/eval-gold.txt"));
    }
    
}
