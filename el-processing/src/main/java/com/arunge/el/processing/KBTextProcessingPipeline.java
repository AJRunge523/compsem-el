package com.arunge.el.processing;

import java.util.Iterator;

import com.arunge.el.api.KBDocument;
import com.arunge.el.mongo.ingest.MongoKnowledgeBaseStore;
import com.mongodb.MongoClient;

public class KBTextProcessingPipeline {

    public static void main(String[] args) {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoKnowledgeBaseStore store = new MongoKnowledgeBaseStore(client, "tackbp");
        Iterator<KBDocument> iter = store.all();
        iter.next();
        KBDocument d = iter.next();
        KBDocumentTextProcessor processor = new KBDocumentTextProcessor();
        processor.process(d);
        
    }
    
}
