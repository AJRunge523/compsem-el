package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

/**
 * 
 *<p>Computes the Jaro-Winkler distance between a pair of strings.<p>
 *
 * @author Andrew Runge
 *
 */
public class JaroWinkler extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    private JaroWinklerDistance dist;
    
    public JaroWinkler(EntityAttribute queryAndCandidateAttribute) {
        super(queryAndCandidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAndCandidateAttribute.name() + "_jaro");
        this.dist = new JaroWinklerDistance();
    }

    public JaroWinkler(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        super(queryAttribute, candidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAttribute.name() + "+" + candidateAttribute.name() + "_jaro");
        this.dist = new JaroWinklerDistance();
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        double editDist = 1.0;
        if(query instanceof StringAttribute && candidate instanceof StringAttribute) { 
            StringAttribute first = (StringAttribute) query;
            StringAttribute second = (StringAttribute) candidate;
            editDist = dist.apply(first.getValueAsStr(), second.getValueAsStr());
        } else if(query instanceof SetAttribute && candidate instanceof StringAttribute) {
            editDist = computeStringDistance((StringAttribute) candidate, (SetAttribute) query);
        } else if(query instanceof StringAttribute && candidate instanceof SetAttribute) {
            editDist = computeStringDistance((StringAttribute) query, (SetAttribute) candidate);
        } else if(query instanceof SetAttribute && candidate instanceof SetAttribute) {
            editDist = computeStringDistance((SetAttribute) query, (SetAttribute) candidate);
        }
        features.put(featureName, editDist);
        return features;
    }
    
    private double computeStringDistance(StringAttribute first, SetAttribute second) {
        if(second.getSetValue() == null || second.getSetValue().isEmpty()) {
            return 0.0;
        }
        double best = 1.0;
        String s1 = first.getValueAsStr();
        for(String s2 : second.getSetValue()) {
            double d = dist.apply(s1, s2);
            if(d < best) {
                best = d;
            }
        }
        return best;
    }
    
    private double computeStringDistance(SetAttribute first, SetAttribute second) {
        if(first.getSetValue() == null || first.getSetValue().isEmpty() ||
                second.getSetValue() == null || second.getSetValue().isEmpty()) {
            return -1.0;
        }
        double best = 1.0;
        for(String s1 : first.getSetValue()) {
            for(String s2 : second.getSetValue()) {
                double d = dist.apply(s1,  s2);
                if(d < best) {
                    best = d;
                }
            }
        }
        return best;
    }
    
}
