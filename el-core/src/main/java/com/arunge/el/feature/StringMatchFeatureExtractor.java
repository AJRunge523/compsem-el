package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class StringMatchFeatureExtractor extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    
    public StringMatchFeatureExtractor(EntityAttribute attribute) {
        super(attribute);
        this.featureName = FeatureDescriptor.of(attribute.name() + "_match");
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> featureValues = new HashMap<>();
        StringAttribute first = (StringAttribute) query;
        StringAttribute second = (StringAttribute) candidate;
        if(first.getValueAsStr().equals(second.getValueAsStr())) {
            featureValues.put(featureName, 1.0);
        } else {
            featureValues.put(featureName, 0.0);
        }
        return featureValues;
    }

    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }
    
}
