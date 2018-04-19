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

public class StringSetOverlapFeatureExtractor extends EntityFeatureExtractor {

    private static Logger LOG = LoggerFactory.getLogger(StringSetOverlapFeatureExtractor.class);
    
    private FeatureDescriptor startsWithCount;
    private FeatureDescriptor endsWithCount;
    private FeatureDescriptor containsCount;
    private FeatureDescriptor startsWithPct;
    private FeatureDescriptor endsWithPct;
    private FeatureDescriptor containsPct;
    
    public StringSetOverlapFeatureExtractor(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        super(queryAttribute, candidateAttribute);
        startsWithCount = FeatureDescriptor.of(queryAttribute.name() + "_" + candidateAttribute.name() + "_" + "sw_count");
        endsWithCount = FeatureDescriptor.of(queryAttribute.name() + "_" + candidateAttribute.name() + "_" + "ew_count");
        containsCount = FeatureDescriptor.of(queryAttribute.name() + "_" + candidateAttribute.name() + "_" + "co_count");
        startsWithPct = FeatureDescriptor.of(queryAttribute.name() + "_" + candidateAttribute.name() + "_" + "sw_pct");
        endsWithPct = FeatureDescriptor.of(queryAttribute.name() + "_" + candidateAttribute.name() + "_" + "ew_pct");
        containsPct = FeatureDescriptor.of(queryAttribute.name() + "_" + candidateAttribute.name() + "_" + "co_pct");
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(startsWithCount, endsWithCount, containsCount,
                startsWithPct, endsWithPct, containsPct);
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
        double startsWith = 0.0;
        double endsWith = 0.0;
        double contains = 0.0;
        Set<String> firstVal = first.getSetValue();
        String secVal = second.getValueAsStr();
        if(firstVal.size() == 0) {
            return features;
        }
        for(String s : firstVal) {
            if(s.startsWith(secVal)) { 
                startsWith += 1;
            }
            if(s.endsWith(secVal)) {
                endsWith += 1;
            }
            if(s.contains(s)) {
                contains += 1;
            }
        }
        features.put(startsWithCount, startsWith / 100.0);
        features.put(endsWithCount, endsWith / 100.0);
        features.put(containsCount, contains / 100.0);
        
        features.put(startsWithPct, startsWith / firstVal.size());
        features.put(endsWithPct, endsWith / firstVal.size());
        features.put(containsPct, contains / firstVal.size());
        
        return features;
    }

}
