package com.arunge.el.nlp.dist;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.arunge.el.api.ContextType;

public class ContextStore {

    private DB db;
    private Map<ContextType, ConcurrentMap> collections;
    
    public static ContextStore getDefault() { 
        return new ContextStore(new File("J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\external_evals\\context_dbs\\contexts.db"), 
                ContextType.FULL_EMB,
                ContextType.FULL_EMB_TFIDF,
                ContextType.WINDOW_EMB,
                ContextType.WINDOW_EMB_TFIDF
                );
    }
    
    public ContextStore(File dbFile, ContextType...types) {
        this.db = DBMaker.fileDB(dbFile)
                .fileMmapEnable()
                .readOnly()
                .make();
        collections = new HashMap<>();
        for(ContextType type : types) {
            collections.put(type, db.hashMap(type.name()).createOrOpen());
        }
    }
    
    public double[] getContext(ContextType type, String id) {
        return (double[]) collections.get(type).get(id);
    }
    
    public void putContext(ContextType type, String id, double[] context) { 
        collections.get(type).put(id, context);
    }
    
    public void putAllContexts(ContextType type, Map<String, double[]> entries) {
        collections.get(type).putAll(entries);
    }
 
    public void close() {
        db.close();        
    }
    
}
