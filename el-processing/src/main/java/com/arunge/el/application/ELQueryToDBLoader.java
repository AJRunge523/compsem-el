package com.arunge.el.application;

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.processing.KBDocumentTextProcessor;
import com.arunge.el.query.QueryDocParser;
import com.arunge.el.query.QuerySetLoader;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class ELQueryToDBLoader {

    public static void main(String[] args) throws Exception {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "el_eval_query_store");
        KBDocumentTextProcessor textProcessor = new KBDocumentTextProcessor();
        QueryDocParser parser = new QueryDocParser();
        
        Iterable<ELQuery> queries = QuerySetLoader.loadTAC2010Eval();
        for(ELQuery query : queries) {
            TextEntity textEntity = query.convertToEntity();
            String docText = parser.getDocText(textEntity.getDocText());
            if(docText.isEmpty()) {
                throw new RuntimeException("Error text was empty.");
            }
            textEntity.setDocText(docText);
            store.insert(textEntity);
            NLPDocument nlp = textProcessor.process(textEntity);
            store.insert(nlp);
        }
    }
    
}
