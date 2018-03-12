package com.arunge.el.mongo.ingest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.arunge.el.api.KBDocument;
import com.mongodb.MongoClient;

public class KBDocumentLoader {

    private static String dataDir = "J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\TAC-KBP 2010\\TAC_2009_KBP_Evaluation_Reference_Knowledge_Base\\data";
    
    private static Logger LOG = LoggerFactory.getLogger(KBDocumentLoader.class);
    
    public static void main(String[] args) throws Exception{
        MongoClient client = new MongoClient("localhost", 27017);
        MongoKnowledgeBaseStore store = new MongoKnowledgeBaseStore(client, "tackbp");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
//        String filename = "src/main/resources/test-kb-entry.xml";
        Stream<Path> files = Files.list(Paths.get(dataDir));
        files.forEach(f -> {
            InputStream in;
            try {
                in = new FileInputStream(f.toFile());
                Document xmlDoc = builder.parse(in);
                KBDocumentParser parser = new KBDocumentParser();
                List<KBDocument> docs = parser.parseDocument(xmlDoc);
                for(KBDocument doc : docs) { 
                    store.insert(doc);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                LOG.error("Error parsing file " + f.getFileName().toString(), e);
            }
            LOG.info("Finished processing " + f.getFileName().toString());
        });
        files.close();
    }
    
}
