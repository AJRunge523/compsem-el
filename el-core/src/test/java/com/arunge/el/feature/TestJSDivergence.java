package com.arunge.el.feature;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
import com.arunge.nlp.api.FeatureDescriptor;

public class TestJSDivergence {

    @Test
    public void test() {
        //Perfect symmetry - value of 0
        JSDivergence extractor = new JSDivergence(EntityAttribute.TOPIC_50);
        double[] d1 = new double[] {0.3, 0.2, 0.1, 0.0, 0.4};
        double[] d2 = new double[] {0.3, 0.2, 0.1, 0.0, 0.4};
        Attribute a1 = SparseVectorAttribute.valueOf(d1);
        Attribute a2 = SparseVectorAttribute.valueOf(d2);
        Map<FeatureDescriptor, Double> features = extractor.extract(a1, a2);
        double d = features.get(FeatureDescriptor.of("TOPIC_50_jsd"));
        assertEquals(d, 0.0, 0.000001);
        
        //Perfect symmetry - square root of upper bound (ln(2))
        double[] d3 = new double[] {0.0, 0.0, 0.0, 0.0, 1.0};
        double[] d4 = new double[] {1.0, 0.0, 0.0, 0.0, 0.0};
        a1 = SparseVectorAttribute.valueOf(d3);
        a2 = SparseVectorAttribute.valueOf(d4);
        features = extractor.extract(a1, a2);
        d = features.get(FeatureDescriptor.of("TOPIC_50_jsd"));
        assertEquals(d, Math.sqrt(Math.log(2)), 0.000001);
    }
    
}
