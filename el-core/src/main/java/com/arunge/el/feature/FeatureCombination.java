package com.arunge.el.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;
import com.arunge.nlp.features.FeatureExtractor;

public class FeatureCombination implements FeatureExtractor<Map<Integer, Double>>{

    private Map<FeatureDescriptor, List<Integer>> featureComboMap;
    
    /**
     * Initializes a FeatureCombination that creates combinations of pairs of features.
     * @param coll1
     * @param coll2
     * @param indexer
     */
    public FeatureCombination(Collection<FeatureDescriptor> coll1, Collection<FeatureDescriptor> coll2, FeatureIndexer indexer) {
        featureComboMap = new HashMap<>();
        for(FeatureDescriptor f1 : coll1) {
            for(FeatureDescriptor f2 : coll2) {
                List<Integer> indexes = new ArrayList<>();
                indexes.add(indexer.getIndex(f1));
                indexes.add(indexer.getIndex(f2));
                featureComboMap.put(createComboFeature(f1, f2), indexes);
            }
        }
        
    }

    /**
     * Initializes a FeatureCombination that creates combinations of feature-triples.
     * @param coll1
     * @param coll2
     * @param coll3
     * @param indexer
     */
    public FeatureCombination(Collection<FeatureDescriptor> coll1, Collection<FeatureDescriptor> coll2, Collection<FeatureDescriptor> coll3, FeatureIndexer indexer) {
        featureComboMap = new HashMap<>();
        for(FeatureDescriptor f1 : coll1) {
            for(FeatureDescriptor f2 : coll2) {
                for(FeatureDescriptor f3 : coll3) { 
                    List<Integer> indexes = new ArrayList<>();
                    indexes.add(indexer.getIndex(f1));
                    indexes.add(indexer.getIndex(f2));
                    indexes.add(indexer.getIndex(f3));
                    featureComboMap.put(createComboFeature(f1, f2, f3), indexes);
                }
            }
        }
    }
    
    private FeatureDescriptor createComboFeature(FeatureDescriptor...feats) {
        return FeatureDescriptor.of(Arrays.stream(feats).map(f -> f.getName()).reduce((a, b) -> a + "++" + b).get());
    }
    
    public Set<FeatureDescriptor> featureNames() {
        return featureComboMap.keySet();
    }
    

    @Override
    public Map<FeatureDescriptor, Double> extractFeatures(Map<Integer, Double> input) {
        Map<FeatureDescriptor, Double> outputFeatures = new HashMap<>();
        for(FeatureDescriptor key : featureComboMap.keySet()) {
            List<Integer> featIndexes = featureComboMap.get(key);
            boolean value = true;
            for(int i : featIndexes) {
                if(!input.containsKey(i) || input.get(i) != 1.0) {
                    value = false;
                    break;
                }
            }
            if(value) {
                outputFeatures.put(key, 1.0);
            } else {
                outputFeatures.put(key, 0.0);
            }
        }
        return outputFeatures;
    }
}
