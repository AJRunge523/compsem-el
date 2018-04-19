package com.arunge.el.nlp.dist;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;

import com.arunge.unmei.iterators.Iterators;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class MongoGloveStore implements GloveStore {

    private MongoCollection<Document> coll;
    
    public MongoGloveStore(MongoClient client, String dbName) {
        this.coll = client.getDatabase(dbName).getCollection("glove");
    }

    public Iterator<Pair<String, float[]>> all(){ 
        return Iterators.map(coll.find().iterator(), d -> {
            @SuppressWarnings("unchecked")
            ArrayList<Double> vec = (ArrayList<Double>) d.get("v");
            float[] vecArr = new float[vec.size()];
            for(int i = 0; i < vec.size(); i++) {
                vecArr[i] = vec.get(i).floatValue();
            }
            return Pair.of(d.getString("_id"), vecArr);
        });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public float[] getWord(String word) {
        Document entry = coll.find(Filters.eq("_id", word)).first();
        if(entry == null) {
            return new float[0];
        } else {
            ArrayList<Double> vec = (ArrayList<Double>) entry.get("v");
            float[] vecArr = new float[vec.size()];
            for(int i = 0; i < vec.size(); i++) {
                vecArr[i] = vec.get(i).floatValue();
            }
            return vecArr;
        }
    }

    @Override
    public float[][] getWords(String[] words) {
        float[][] vecs = new float[words.length][];
        for(int i = 0; i < words.length; i++) {
            vecs[i] = getWord(words[i]);
        }
        return vecs;
    }
    
}
