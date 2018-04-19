package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

import smile.math.distance.JensenShannonDistance;

public class JSDivergence extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    private JensenShannonDistance jsDist;
    
    public JSDivergence(EntityAttribute queryAndCandidateAttribute) {
        super(queryAndCandidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAndCandidateAttribute.name() + "_jsd");
        this.jsDist = new JensenShannonDistance();
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        if(!verifyAttributes(query, candidate)) {
            return features;
        }
        SparseVectorAttribute first = (SparseVectorAttribute) query;
        SparseVectorAttribute second = (SparseVectorAttribute) candidate;
        Map<Integer, Double> firstVal = first.getValue();
        Map<Integer, Double> secVal = second.getValue();
        int max = -1;
        for(Integer key : firstVal.keySet()) {
            if(key > max) {
                max = key;
            }
        }
        for(Integer key : secVal.keySet()) {
            if(key > max) {
                max = key;
            }
        }
        max = max + 1;
        double[] firstArr = convert(firstVal, max);
        double[] secArr = convert(secVal, max);
        double jsd = jsDist.d(firstArr, secArr);
        features.put(featureName, jsd);
        return features;
    }

    private double[] convert(Map<Integer, Double> sparseVec, int max) {
        double[] array = new double[max];
        for(Integer key : sparseVec.keySet()) {
            array[key] = sparseVec.get(key);
        }
        return array;
    }
    
    private boolean verifyAttributes(Attribute a1, Attribute a2) {
        if(!(a1 instanceof SparseVectorAttribute) || !(a2 instanceof SparseVectorAttribute)) {
            return false;
        }
        return true;
    }
    
}
