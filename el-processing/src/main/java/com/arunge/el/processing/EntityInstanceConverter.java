package com.arunge.el.processing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arunge.el.api.KBEntity;
import com.arunge.el.feature.EntityFeatureExtractor;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;

public class EntityInstanceConverter {

    private List<EntityFeatureExtractor> extractors;
    
    private FeatureIndexer indexer;
    
    public EntityInstanceConverter(List<EntityFeatureExtractor> extractors) {
        this.extractors = extractors;
        this.indexer = new FeatureIndexer();
        for(EntityFeatureExtractor extractor : extractors) { 
            for(FeatureDescriptor desc : extractor.featureNames()) {
                this.indexer.getOrAdd(desc);
            }
        }
    }
    
    public Map<Integer, Double> convert(KBEntity query, KBEntity candidate) {
        Map<Integer, Double> instance = new HashMap<>();
        for(EntityFeatureExtractor extractor : extractors) {
            Map<FeatureDescriptor, Double> features = extractor.extractFeatures(query, candidate);
            for(FeatureDescriptor key : features.keySet()) {
                int index = indexer.getIndex(key);
                instance.put(index, features.get(key));
            }
        }
        return instance;
        
    }
    
    
    
}
