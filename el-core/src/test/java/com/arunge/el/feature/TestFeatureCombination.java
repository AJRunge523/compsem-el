package com.arunge.el.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;

public class TestFeatureCombination {

    @Test
    public void testPair() { 
        BinningFeature binEdit = new BinningFeature(new LevenshteinEditDistance(EntityAttribute.NAME));
        StringMatchFeatureExtractor string = new StringMatchFeatureExtractor(EntityAttribute.ENTITY_TYPE);
        FeatureIndexer indexer = new FeatureIndexer();
        for(FeatureDescriptor d : binEdit.featureNames()) {
            indexer.getOrAdd(d);
        }
        for(FeatureDescriptor d : string.featureNames()) {
            indexer.getOrAdd(d);
        }
        FeatureCombination combo = new FeatureCombination(binEdit.featureNames(), string.featureNames(), indexer);
        assertEquals(combo.featureNames().size(), 4);
        KBEntity e1 = new KBEntity("a");
        e1.setName("John Smith");
        e1.setType(EntityType.PERSON);
        
        KBEntity e2 = new KBEntity("b");
        e2.setName("Jon Smith");
        e2.setType(EntityType.PERSON);
        Map<Integer, Double> instance = new HashMap<>();
        
        Map<FeatureDescriptor, Double> vals = binEdit.extractFeatures(e1, e2);
        for(FeatureDescriptor d : vals.keySet()) {
            instance.put(indexer.getIndex(d), vals.get(d));
        }
        vals = string.extractFeatures(e1, e2);
        for(FeatureDescriptor d : vals.keySet()) {
            instance.put(indexer.getIndex(d), vals.get(d));
        }
        vals = combo.extractFeatures(instance);
        int sum = 0;
        for(FeatureDescriptor f : combo.featureNames()) { 
            assertTrue(vals.containsKey(f));
            sum += vals.get(f);
        }
        assertEquals(sum, 1.0, 0.00000001);
        assertEquals(vals.get(FeatureDescriptor.of("NAME_edit_<=0.25++ENTITY_TYPE_match")).doubleValue(), 1.0, 0.000001); 
//        assertTrue(vals.get(FeatureDescriptor.of(name)))
    }
    
    @Test
    public void testTriple() {
        BinningFeature binEdit = new BinningFeature(new LevenshteinEditDistance(EntityAttribute.NAME));
        StringMatchFeatureExtractor string = new StringMatchFeatureExtractor(EntityAttribute.ENTITY_TYPE);
        BinningFeature lcs = new BinningFeature(new LongestCommonSubstringDistance(EntityAttribute.NAME));
        FeatureIndexer indexer = new FeatureIndexer();
        for(FeatureDescriptor d : binEdit.featureNames()) {
            indexer.getOrAdd(d);
        }
        for(FeatureDescriptor d : string.featureNames()) {
            indexer.getOrAdd(d);
        }
        for(FeatureDescriptor d : lcs.featureNames()) {
            indexer.getOrAdd(d);
        }
        FeatureCombination combo = new FeatureCombination(binEdit.featureNames(), string.featureNames(), lcs.featureNames(), indexer);
        assertEquals(combo.featureNames().size(), 16);
        KBEntity e1 = new KBEntity("a");
        e1.setName("John Smith");
        e1.setType(EntityType.PERSON);
        
        KBEntity e2 = new KBEntity("b");
        e2.setName("Jon Smith");
        e2.setType(EntityType.PERSON);
        Map<Integer, Double> instance = new HashMap<>();
        
        Map<FeatureDescriptor, Double> vals = binEdit.extractFeatures(e1, e2);
        for(FeatureDescriptor d : vals.keySet()) {
            instance.put(indexer.getIndex(d), vals.get(d));
        }
        vals = string.extractFeatures(e1, e2);
        for(FeatureDescriptor d : vals.keySet()) {
            instance.put(indexer.getIndex(d), vals.get(d));
        }
        vals = lcs.extractFeatures(e1, e2);
        for(FeatureDescriptor d : vals.keySet()) {
            instance.put(indexer.getIndex(d), vals.get(d));
        }
        vals = combo.extractFeatures(instance);
        int sum = 0;
        for(FeatureDescriptor f : combo.featureNames()) { 
            assertTrue(vals.containsKey(f));
            sum += vals.get(f);
        }
        assertEquals(sum, 1.0, 0.00000001);
        assertEquals(vals.get(FeatureDescriptor.of("NAME_edit_<=0.25++ENTITY_TYPE_match++NAME_lcs_<=0.25")).doubleValue(), 1.0, 0.000001); 
    }
    
}
