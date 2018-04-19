package com.arunge.el.nlp.dist;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDBGloveStore implements GloveStore {

    private DB db;
    private ConcurrentMap map;
    
    public MapDBGloveStore(String dbFile) {
        db = DBMaker.fileDB(dbFile).fileMmapEnable()
                .readOnly()
                .make();
        map = db.hashMap("map").open();
    }
    
    @Override
    public float[] getWord(String word) {
        return (float[] )map.get(word);
    }

    @Override
    public float[][] getWords(String[] words) {
        return Arrays.stream(words).map(w -> getWord(w)).toArray(float[][]::new);
    }

    @Override
    public Iterator<Pair<String, float[]>> all() {
        throw new UnsupportedOperationException("Currently don't support this.");
    }

}
