package com.arunge.el.nlp.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.nlp.dist.MongoGloveStore;
import com.mongodb.MongoClient;

public class ConvertGloveToMapDB {

    private static Logger LOG = LoggerFactory.getLogger(ConvertGloveToMapDB.class);
    
    public static void main(String[] args) {
        DB db = DBMaker.fileDB("glove.db").fileMmapEnable().make();
        ConcurrentMap map = db.hashMap("map").createOrOpen();
        
        MongoClient client = new MongoClient("localhost", 27017);
        MongoGloveStore store = new MongoGloveStore(client, "glove_vectors");
        Iterator<Pair<String, float[]>> vectors = store.all();
        int numLoaded = 0;
        Map<String, float[]> vecs = new HashMap<>();
        while(vectors.hasNext()) {
            
            Pair<String, float[]> vec = vectors.next();
            vecs.put(vec.getLeft(), vec.getValue());
//            map.put(vec.getLeft(), vec.getRight());
            numLoaded += 1;
            if(numLoaded % 10000 == 0) {
                map.putAll(vecs);
                vecs.clear();
                LOG.info("Processed {} entries.", numLoaded);
            }
        }
        
        db.close();
        client.close();
    }
    
}
