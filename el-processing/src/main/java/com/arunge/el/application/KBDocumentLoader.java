package com.arunge.el.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.TextEntity;
import com.arunge.el.kb.KBDocumentParser;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class KBDocumentLoader {

    private static String dataDir = "J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\TAC-KBP KB Train and Test\\TAC_2009_KBP_Evaluation_Reference_Knowledge_Base\\data";
    
    private static Logger LOG = LoggerFactory.getLogger(KBDocumentLoader.class);
    
    public static void main(String[] args) throws Exception{
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "entity_store");
        Stream<Path> files = Files.list(Paths.get(dataDir));
        files.forEach(f -> {
            try {
                KBDocumentParser parser = new KBDocumentParser();
                List<TextEntity> docs = parser.parseDocument(f.toFile());
                for(TextEntity doc : docs) { 
                    store.insert(doc);
                }
            } catch (Exception e) {
                LOG.error("Error parsing file " + f.getFileName().toString(), e);
            }
            LOG.info("Finished processing " + f.getFileName().toString());
        });
        files.close();
        
    }
    
}
