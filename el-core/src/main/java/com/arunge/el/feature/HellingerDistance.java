package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.DenseVectorAttribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class HellingerDistance extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    
    public HellingerDistance(EntityAttribute queryAndCandidateAttribute) { 
        super(queryAndCandidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAndCandidateAttribute.name() + "_hel");
    }
    
    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        double cosine = 0.0;
        if(query instanceof SparseVectorAttribute && candidate instanceof SparseVectorAttribute) {
            Map<Integer, Double> first = ((SparseVectorAttribute) query).getValue();
            Map<Integer, Double> second = ((SparseVectorAttribute) candidate).getValue();
            cosine = computeHellinger(first, second);
        } else if(query instanceof DenseVectorAttribute && candidate instanceof DenseVectorAttribute) {
            double[] first = ((DenseVectorAttribute) query).getValue();
            double[] second = ((DenseVectorAttribute) candidate).getValue();
            cosine = computeHellinger(first, second);
        }
        features.put(featureName, cosine);
        return features;
    }

    private static double computeHellinger(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        if(v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for(Integer key : v1.keySet()) {
            if(v2.containsKey(key)) { 
                sum += Math.pow(Math.sqrt(v1.get(key)) - Math.sqrt(v2.get(key)), 2);
            } else {
                sum += v1.get(key);
            }
        }
        for(Integer key : v2.keySet()) {
            if(v1.containsKey(key)) {
                continue;
            } else {
                sum += v2.get(key);
            }
        }
        return 0.5 * sum;
    }
    
    private static double computeHellinger(double[] v1, double[] v2) {
        if(v1 == null || v1.length == 0 || v2 == null || v2.length == 0 || v1.length != v2.length) {
            return 0.0;
        }
        double sum = 0.0;
        for(int i = 0; i < v1.length; i++) {
            sum += Math.pow(Math.sqrt(v1[i]) - Math.sqrt(v2[i]), 2);
        }
        return 0.5 * sum;
    }
 
    public static void main(String[] args) { 
        double[] a = new double[] {0.0, 0.7, 0.3, 0.0};
        double[] b = new double[] {0.0, 0.3, 0.2, 0.5};
        SparseVectorAttribute sva = SparseVectorAttribute.valueOf(a);
        SparseVectorAttribute svb = SparseVectorAttribute.valueOf(b);
        
        double val = computeHellinger(a, b);
        System.out.println(val);
        val = computeHellinger(sva.getValue(), svb.getValue());
        System.out.println(val);
        
    }
    
}
