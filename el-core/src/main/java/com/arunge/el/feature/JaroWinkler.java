package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.attribute.Attribute;
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
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        StringAttribute first = (StringAttribute) query;
        StringAttribute second = (StringAttribute) candidate;
        features.put(featureName, dist.apply(first.getValueAsStr(), second.getValueAsStr()));
        return features;
    }
    
}
