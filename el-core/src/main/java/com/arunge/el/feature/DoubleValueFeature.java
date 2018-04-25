package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.DoubleAttribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

/**
 * 
 *<p>Extracts a single double valued attribute from the candidate entity as a feature.<p>
 *
 * @author Andrew Runge
 *
 */
public class DoubleValueFeature extends EntityFeatureExtractor {

    private FeatureDescriptor featDesc;
    
    public DoubleValueFeature(EntityAttribute attr) {
        super(attr);
        this.featDesc = FeatureDescriptor.of(attr.name() + "_val");
    }
    
    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featDesc);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        if(candidate instanceof DoubleAttribute) {
            features.put(featDesc, ((DoubleAttribute) candidate).getValue());
        }
        return features;
    }

}
