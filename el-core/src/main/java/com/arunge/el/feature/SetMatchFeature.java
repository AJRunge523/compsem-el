package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class SetMatchFeature extends EntityFeatureExtractor {

    private FeatureDescriptor featDesc;
    
    public SetMatchFeature(EntityAttribute attr) { 
        super(attr);
        this.featDesc = FeatureDescriptor.of(attr.name() + "_overlap");
    }
    
    public SetMatchFeature(EntityAttribute queryAttr, EntityAttribute candAttr) {
        super(queryAttr, candAttr);
        this.featDesc = FeatureDescriptor.of(queryAttr.name() + "_" + candAttr + "_overlap");
    }
    
    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featDesc);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        if(query instanceof SetAttribute && candidate instanceof SetAttribute) { 
            SetAttribute first = (SetAttribute) query;
            SetAttribute second = (SetAttribute) candidate;
            Set<String> overlap = Sets.intersection(first.getSetValue(), second.getSetValue());
            if(overlap.size() > 0) {
                features.put(featDesc, 1.0);
            } else {
                features.put(featDesc, 0.0);
            }
        }
        return features;
    }

    
}
