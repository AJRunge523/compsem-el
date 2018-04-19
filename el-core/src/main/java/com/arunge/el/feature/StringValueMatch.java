package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class StringValueMatch extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    private String queryValue;
    private String candidateValue;
    
    public StringValueMatch(EntityAttribute attribute, String value) {
        super(attribute);
        this.featureName = FeatureDescriptor.of(attribute.name() + "_" + value + "_match");
        this.queryValue = value;
        this.candidateValue = value;
    }
    
    public StringValueMatch(EntityAttribute attribute, String queryValue, String candidateValue) {
        super(attribute);
        this.featureName = FeatureDescriptor.of(attribute.name() + "_" + queryValue + "_" + candidateValue + "_match");
        this.queryValue = queryValue;
        this.candidateValue = candidateValue;
    }
    
    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        String first = query.getValueAsStr();
        String second = candidate.getValueAsStr();
        if(first.equals(queryValue) && second.equals(candidateValue)) { 
            features.put(featureName, 1.0);
        } else {
            features.put(featureName, 0.0);
        }
        return features;
    }

}
