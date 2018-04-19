package com.arunge.el.nlp.wiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.el.store.mongo.MongoNLPFields;
import com.mongodb.MongoClient;

public class WikiAliasLoader {

    private static Logger LOG = LoggerFactory.getLogger(WikiAliasLoader.class);
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File aliasFile = new File("J:/Education/CMU/2018/Spring/Computational Semantics/Entity Linking/external_evals/aliases/redir_aliases_final.txt");
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "entity_store");
        
        int numProcessed = 0;
        try(BufferedReader reader = new BufferedReader(new FileReader(aliasFile))) {
            String line = "";
            while((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if(parts.length == 1) {
                    continue;
                }
                String id = parts[0];
                Set<String> aliases = new HashSet<>();
                for(int i = 1; i < parts.length; i++) {
                    String alias = parts[i].trim();
                    if(alias.startsWith("Talk:")) {
                        continue;
                    } else if(alias.startsWith("List of ")) {
                        continue;
                    }
                    
                    aliases.add(alias);
                    if(alias.contains("(")) {
                        //Add the same alias without parentheses
                        aliases.add(parts[i].replaceAll("\\(.*\\)", "").trim());
                    }
                }
                store.updateNLPDocument(id, MongoNLPFields.WIKI_ALIASES, aliases);
                numProcessed += 1;
                if(numProcessed % 10000 == 0) { 
                    LOG.info("Number of documents processed: {}", numProcessed);
                }
            }
        }
        
        
    }
    
}
