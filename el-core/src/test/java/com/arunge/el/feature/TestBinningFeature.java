package com.arunge.el.feature;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.nlp.api.FeatureDescriptor;

public class TestBinningFeature {

    @Test
    public void test() {
        BinningFeature feature = new BinningFeature(new LevenshteinEditDistance(EntityAttribute.NAME));
        KBEntity e1 = new KBEntity("e1");
        e1.setName("bags");
        KBEntity e2 = new KBEntity("e1");
        e2.setName("bugs");
        Map<FeatureDescriptor, Double> features = feature.extractFeatures(e1, e2);
        assertEquals(features.size(), 6);
        double total = 0.0;
        for(FeatureDescriptor f : feature.featureNames()) { 
            total += features.get(f);
        }
        assertEquals(features.get(FeatureDescriptor.of("NAME_edit_<=0.5")), 1.0, 0.00001);
        assertEquals(total, 1.0, 0.0000001);
    }
    
    @Test
    public void test2() {
        BinningFeature feature = new BinningFeature(new LevenshteinEditDistance(EntityAttribute.NAME), 0.0f, 1.0f, 8);
        KBEntity e1 = new KBEntity("e1");
        e1.setName("bags");
        KBEntity e2 = new KBEntity("e1");
        e2.setName("bugs");
        Map<FeatureDescriptor, Double> features = feature.extractFeatures(e1, e2);
        assertEquals(features.size(), 10);
        double total = 0.0;
        for(FeatureDescriptor f : feature.featureNames()) { 
            total += features.get(f);
        }
        assertEquals(features.get(FeatureDescriptor.of("NAME_edit_<=0.375")), 1.0, 0.00001);
        assertEquals(total, 1.0, 0.0000001);
    }
    
}
