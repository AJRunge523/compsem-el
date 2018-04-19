package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

/**
 * 
 *<p>Tests for intersection between the value of a string attribute and the values of a set attribute.<p>
 *
 * @author Andrew Runge
 *
 */
public class StringSetMatchFeatureExtractor extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    
    private static Logger LOG = LoggerFactory.getLogger(StringSetMatchFeatureExtractor.class);
    
    public StringSetMatchFeatureExtractor(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        super(queryAttribute, candidateAttribute);
        featureName = FeatureDescriptor.of(queryAttribute.name() + "+" + candidateAttribute.name() + "_strset");
    }
    
    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        SetAttribute first = null;
        if(query instanceof SetAttribute) {
            first = (SetAttribute) query;
        } else if(candidate instanceof SetAttribute) {
            first = (SetAttribute) candidate;
        } 
        StringAttribute second = null;
        if(query instanceof StringAttribute) {
            second = (StringAttribute) query;
        } else if(candidate instanceof StringAttribute) {
            second = (StringAttribute) candidate;
        }
        if(first == null || second == null) {
            LOG.warn("Expected comparison between string and set attributes. Returning no values.");
            return new HashMap<>();
        }
        if(first.getSetValue().contains(second.getValueAsStr())) {
            features.put(featureName, 1.0);
        } else {
            features.put(featureName, 0.0);
        }
        return features;
    }

}
