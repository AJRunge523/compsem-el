package com.arunge.el.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.processing.KBDocumentTextProcessor;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class KBNLPAnalyzer {

    private static Logger LOG = LoggerFactory.getLogger(KBNLPAnalyzer.class);
    
    public static void main(String[] args) throws Exception {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore es = new MongoEntityStore(client, "entity_store");
        KBDocumentTextProcessor processor = new KBDocumentTextProcessor();
        try(CloseableIterator<TextEntity> textDocs = es.allKBText()) { 
            int numProcessed = 0;
            while(textDocs.hasNext()) {
                NLPDocument doc = processor.process(textDocs.next());
                es.insert(doc);
                numProcessed += 1;
                if(numProcessed % 10000 == 0) {
                    LOG.info("Processed {} documents", numProcessed);
                }
            }

        }
        LOG.info("Finished applying NLP pipeline to documents.");
    }
}
