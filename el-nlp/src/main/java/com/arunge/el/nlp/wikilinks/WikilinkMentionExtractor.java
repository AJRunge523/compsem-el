package com.arunge.el.nlp.wikilinks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikilinkMentionExtractor {

    private static Logger LOG = LoggerFactory.getLogger(WikilinkMentionExtractor.class);
    
    public static void main(String[] args) throws IOException {
        File dataDir = new File("J://wikilinks//data");
        Map<String, Set<String>> linksToMentions = new HashMap<>();
        for(File f : dataDir.listFiles()) {
            LOG.info("Processing file {}", f.getName());
            try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
                String line = "";
                while((line = reader.readLine()) != null) { 
                    if(!line.startsWith("MENTION")) {
                        continue;
                    }
                    String[] parts = line.split("\t");
                    String mention = parts[1];
                    String link = parts[3];
//                    link = link.replaceAll("http://en.wikipedia.org/wiki/", "");
                    if(!linksToMentions.containsKey(link)) {
                        linksToMentions.put(link, new HashSet<>());
                    }
                    if(linksToMentions.containsKey(link)) {
                        linksToMentions.get(link).add(mention);    
                    }
                }
            }
            
//            break;
        }
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output/alias_links.txt")));
        for(String link : linksToMentions.keySet()) {
            writer.write(link);
            for(String mention : linksToMentions.get(link)) { 
                writer.write("\t" + mention);
            }
            writer.write("\n");
        }
        writer.close();
    }
    
}
