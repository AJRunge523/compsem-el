package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class CosineSimilarity extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    
    public CosineSimilarity(EntityAttribute queryAndCandidateAttribute) {
        super(queryAndCandidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAndCandidateAttribute.name() + "_cosine");
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        Map<Integer, Double> first = ((SparseVectorAttribute) query).getValue();
        Map<Integer, Double> second = ((SparseVectorAttribute) candidate).getValue();
        
        double cosine = computeCosine(first, second);
        features.put(featureName, cosine);
        return features;
    }

    private static double computeCosine(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        if(v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        double firstNorm = 0.0;
        double secondNorm = 0.0;
        double dot = 0.0;
        for(Integer key : v1.keySet()) {
            if(v2.containsKey(key)) { 
                dot += (v1.get(key) * v2.get(key));
            }
            firstNorm += (v1.get(key) * v1.get(key));
        }
        for(Integer key : v2.keySet()) {
            secondNorm += (v2.get(key) * v2.get(key));
        }
        firstNorm = Math.sqrt(firstNorm);
        secondNorm = Math.sqrt(secondNorm);
        double cosine = dot / (firstNorm * secondNorm);
        return cosine;
    }
    
    public static void main(String[] args) {
        Map<Integer, Double> a = new HashMap<>();
        a.put(1, 2.0);
        a.put(2, 3.0);
        Map<Integer, Double> b = new HashMap<>();
        b.put(1, 2.0);
        b.put(2,  4.0);
        System.out.println(computeCosine(a, b));
    }
    
}
