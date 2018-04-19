package com.arunge.el.processing.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class GoldTrainDevSplitter {

    public static void main(String[] args) throws IOException { 
        Set<String> devIds = Sets.newHashSet(Files.readLines(new File("src/main/resources/dev-ids.txt"), Charsets.UTF_8));
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(new File("src/main/resources/train-gold.txt")));
        BufferedWriter devWriter = new BufferedWriter(new FileWriter(new File("src/main/resources/dev-gold.txt")));
        
        try(BufferedReader reader = new BufferedReader(new FileReader(new File("src/main/resources/train-gold-full.txt")))) {
            String line = "";
            while((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                String id = parts[0];
                if(devIds.contains(id)) {
                    devWriter.write(line + "\n");
                } else {
                    trainWriter.write(line + "\n");
                }
            }
        }
        trainWriter.close();
        devWriter.close();
    }
    
}
