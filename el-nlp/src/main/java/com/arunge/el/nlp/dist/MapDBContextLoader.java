package com.arunge.el.nlp.dist;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class MapDBContextLoader {

    private static Logger LOG = LoggerFactory.getLogger(MapDBContextLoader.class);
    
    public static void main2(String[] args) {
        ContextType[] types = new ContextType[] {ContextType.FULL_EMB, ContextType.FULL_EMB_TFIDF, ContextType.WINDOW_EMB, ContextType.WINDOW_EMB_TFIDF};
        
        ContextStore contextStore = new ContextStore(new File("J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\external_evals\\context_dbs\\contexts.db"), types);
        
        Map<ContextType, Map<String, double[]>> contexts = new HashMap<>();
        for(ContextType type : types) {
            contexts.put(type,  new HashMap<>());
        }
        
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore entityStore = MongoEntityStore.kbStore(client);
        CloseableIterator<NLPDocument> docs = entityStore.allNLPDocuments();
        int numProcessed = 0;
        while(docs.hasNext()) {
            NLPDocument doc = docs.next();
            for(ContextType type : types) {
                Map<Integer, Double> dist = doc.getDistribution(type);
                if(dist != null) {
                    double[] vector = convertToVector(dist);
                    contexts.get(type).put(doc.getId(), vector);
                }
            }
            if(contexts.get(ContextType.FULL_EMB).size() > 10000) {
                for(ContextType type : types) {
                    contextStore.putAllContexts(type, contexts.get(type));
                    contexts.get(type).clear();
                }
            }
            numProcessed += 1;
            if(numProcessed % 10000 == 0) { 
                LOG.info("Processed {} documents.", numProcessed);
            }
        }
        for(ContextType type : types) {
            if(contexts.get(type).size() > 0) {
                System.out.println("Flushing contexts for type " + type.toString());
                contextStore.putAllContexts(type, contexts.get(type));
            }
        }
        contextStore.close();
    }
    
    public static void main(String[] args) { 
        ContextType[] types = new ContextType[] {ContextType.FULL_EMB, ContextType.FULL_EMB_TFIDF, ContextType.WINDOW_EMB, ContextType.WINDOW_EMB_TFIDF};
        
        ContextStore contextStore = new ContextStore(new File("J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\external_evals\\context_dbs\\contexts.db"), types);
        
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore entityStore = MongoEntityStore.kbStore(client);
        CloseableIterator<TextEntity> text = entityStore.allKBText();
        while(text.hasNext()) {
            TextEntity doc = text.next();
            System.out.println(doc.getId());
            for(ContextType type : types) {
                double[] dist = contextStore.getContext(type, doc.getId());
                if(dist == null) { 
                    throw new RuntimeException("Boo");
                }
                System.out.println(type + ", " + dist[299]);
            }
        }
        contextStore.close();
    }
    
    private static double[] convertToVector(Map<Integer, Double> dist) { 
        double[] vector = new double[300];
        for(Integer i : dist.keySet()) {
            vector[i] = dist.get(i);
        }
        return vector;
    }
    
}
