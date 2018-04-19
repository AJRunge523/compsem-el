package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class StringOverlapFeatureExtractor extends EntityFeatureExtractor {

    private FeatureDescriptor startsWithCand;
    private FeatureDescriptor endsWithCand;
    private FeatureDescriptor containsCand;
    private FeatureDescriptor startsWithQuery;
    private FeatureDescriptor endsWithQuery;
    private FeatureDescriptor containsQuery;
    
    public StringOverlapFeatureExtractor(EntityAttribute attribute) {
        super(attribute);
        startsWithCand = FeatureDescriptor.of(attribute.name() + "_" + "sw_c");
        endsWithCand = FeatureDescriptor.of(attribute.name() + "_" + "ew_c");
        containsCand = FeatureDescriptor.of(attribute.name() + "_" + "co_c");
        startsWithQuery = FeatureDescriptor.of(attribute.name() + "_" + "sw_q");
        endsWithQuery = FeatureDescriptor.of(attribute.name() + "_" + "ew_q");
        containsQuery = FeatureDescriptor.of(attribute.name() + "_" + "co_q");
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(startsWithCand, endsWithCand, containsCand,
                startsWithQuery, endsWithQuery, containsQuery);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> featureValues = new HashMap<>();
        String queryVal= query.getValueAsStr();
        String candVal = candidate.getValueAsStr();
        addFeature(featureValues, startsWithCand, queryVal.startsWith(candVal));
        addFeature(featureValues, endsWithCand, queryVal.endsWith(candVal));
        addFeature(featureValues, containsCand, queryVal.contains(candVal));
        addFeature(featureValues, startsWithQuery, candVal.startsWith(queryVal));
        addFeature(featureValues, endsWithQuery, candVal.endsWith(queryVal));
        addFeature(featureValues, containsQuery, candVal.contains(queryVal));
        return featureValues;
    }

    private void addFeature(Map<FeatureDescriptor, Double> features, FeatureDescriptor key, boolean test) {
        if(test) {
            features.put(key, 1.0);
        } else {
            features.put(key, 0.0);
        }
    }
}
