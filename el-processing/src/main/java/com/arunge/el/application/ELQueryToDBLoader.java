package com.arunge.el.application;

import java.io.File;

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.TextEntity;
import com.arunge.el.processing.utils.GoldInfoSupplier;
import com.arunge.el.query.QueryDocParser;
import com.arunge.el.query.QuerySetLoader;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class ELQueryToDBLoader {

    public static void main(String[] args) throws Exception {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.trainStore(client);
        GoldInfoSupplier supplier = GoldInfoSupplier.goldTrain();
        QueryDocParser parser = new QueryDocParser();
        
        Iterable<ELQuery> queries = QuerySetLoader.loadTAC2010Train();
        for(ELQuery query : queries) {
            TextEntity textEntity = query.convertToEntity();
            String docText = parser.getDocText(textEntity.getDocText());
            if(docText.isEmpty()) {
                throw new RuntimeException("Error text was empty.");
            }
            textEntity.setDocText(docText);
            textEntity.putMetadata("doc_path", new File(query.getDocPath()).getName());
            textEntity.clearMetadata("gold");
            textEntity.putMetadata("gold", supplier.getGoldEntity(textEntity.getId()));
            textEntity.putMetadata("goldNER", supplier.getGoldNER(textEntity.getId()));
            store.insert(textEntity);
//            NLPDocument nlp = textProcessor.process(textEntity);
//            store.insert(nlp);
        }
    }
    
}
