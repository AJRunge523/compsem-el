package com.arunge.el.nlp.dist;

import java.util.Iterator;

import org.apache.commons.lang3.tuple.Pair;

public interface GloveStore {

    public Iterator<Pair<String, float[]>> all();
    
    public float[] getWord(String word);
    
    public float[][] getWords(String[] words);
    
}
