package com.arunge.el.processing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.feature.BinningFeature;
import com.arunge.el.feature.CosineSimilarity;
import com.arunge.el.feature.EntityFeatureExtractor;
import com.arunge.el.feature.FeatureCombination;
import com.arunge.el.feature.JSDivergence;
import com.arunge.el.feature.JaroWinkler;
import com.arunge.el.feature.LevenshteinEditDistance;
import com.arunge.el.feature.LongestCommonSubstringDistance;
import com.arunge.el.feature.StringMatchFeatureExtractor;
import com.arunge.el.feature.StringOverlapFeatureExtractor;
import com.arunge.el.feature.StringSetMatchFeatureExtractor;
import com.arunge.el.feature.StringSetOverlapFeatureExtractor;
import com.arunge.el.feature.StringValueMatch;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureIndexer;
import com.google.common.collect.Lists;

public class EntityPairInstanceConverter {

    private static Logger LOG = LoggerFactory.getLogger(EntityPairInstanceConverter.class);
    
    private Collection<EntityFeatureExtractor> extractors;
    
    private Collection<FeatureCombination> featureCombinations;
    
    private FeatureIndexer indexer;
    
    public static EntityPairInstanceConverter currentSet() { 
        List<EntityFeatureExtractor> extractors = new ArrayList<>();
        List<List<Collection<EntityFeatureExtractor>>> featureCombinations = new ArrayList<>();
        
        
        //Text Features
        List<EntityFeatureExtractor> nameFeatures = new ArrayList<>();

        nameFeatures.add(new StringMatchFeatureExtractor(EntityAttribute.NAME));
        nameFeatures.add(new StringMatchFeatureExtractor(EntityAttribute.CLEANSED_NAME));
        nameFeatures.add(new StringOverlapFeatureExtractor(EntityAttribute.NAME));
        nameFeatures.add(new StringOverlapFeatureExtractor(EntityAttribute.CLEANSED_NAME));
        nameFeatures.add(new LevenshteinEditDistance(EntityAttribute.CLEANSED_NAME));
        nameFeatures.add(new JaroWinkler(EntityAttribute.CLEANSED_NAME));
        nameFeatures.add(new LongestCommonSubstringDistance(EntityAttribute.NAME));
        nameFeatures.add(new LongestCommonSubstringDistance(EntityAttribute.CLEANSED_NAME));

        //Alias Features
        List<EntityFeatureExtractor> aliasFeatures = new ArrayList<>();
        aliasFeatures.add(new StringSetMatchFeatureExtractor(EntityAttribute.NAME, EntityAttribute.ALIASES));
        aliasFeatures.add(
                new StringSetMatchFeatureExtractor(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES));
        List<EntityFeatureExtractor> realAliasFeatures = new ArrayList<>();
        
        realAliasFeatures.add(new StringSetOverlapFeatureExtractor(EntityAttribute.NAME, EntityAttribute.ALIASES));
        realAliasFeatures.add(
                new StringSetOverlapFeatureExtractor(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES));
        realAliasFeatures.add(new LevenshteinEditDistance(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES));
        realAliasFeatures.add(new JaroWinkler(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES));
        realAliasFeatures.add(new LongestCommonSubstringDistance(EntityAttribute.NAME, EntityAttribute.ALIASES));
        realAliasFeatures.add(
                new LongestCommonSubstringDistance(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES));
        realAliasFeatures.add(new StringMatchFeatureExtractor(EntityAttribute.ACRONYM));
        realAliasFeatures.add(new StringSetMatchFeatureExtractor(EntityAttribute.ACRONYM, EntityAttribute.ALIASES));
        
        // Binned string features
        List<EntityFeatureExtractor> binnedNameFeatures = new ArrayList<>();
        binnedNameFeatures.add(new BinningFeature(new LevenshteinEditDistance(EntityAttribute.CLEANSED_NAME)));
        binnedNameFeatures.add(new BinningFeature(new JaroWinkler(EntityAttribute.CLEANSED_NAME)));
        binnedNameFeatures.add(new BinningFeature(new LongestCommonSubstringDistance(EntityAttribute.NAME)));
        binnedNameFeatures.add(new BinningFeature(new LongestCommonSubstringDistance(EntityAttribute.CLEANSED_NAME)));
        
        // Binned alias features
        List<EntityFeatureExtractor> binnedAliasFeatures = new ArrayList<>();
        binnedAliasFeatures.add(new BinningFeature(new StringSetOverlapFeatureExtractor(EntityAttribute.NAME, EntityAttribute.ALIASES), 0.0f, 0.2f, 5));
        binnedAliasFeatures.add(new BinningFeature(
                new StringSetOverlapFeatureExtractor(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES), 0.0f, 0.2f, 5));
        binnedAliasFeatures.add(new BinningFeature(new LevenshteinEditDistance(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES)));
        binnedAliasFeatures.add(new BinningFeature(new JaroWinkler(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES)));
        binnedAliasFeatures.add(new BinningFeature(new LongestCommonSubstringDistance(EntityAttribute.NAME, EntityAttribute.ALIASES)));
        binnedAliasFeatures.add(new BinningFeature(
                new LongestCommonSubstringDistance(EntityAttribute.CLEANSED_NAME, EntityAttribute.CLEANSED_ALIASES)));
        
        //Features using entity coref

        //Semantic-y features
        List<EntityFeatureExtractor> nerFeatures = new ArrayList<>();
        nerFeatures.add(new StringMatchFeatureExtractor(EntityAttribute.ENTITY_TYPE));
        
        List<EntityFeatureExtractor> nerTypeFeatures = new ArrayList<>();
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "PERSON"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "GPE"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "ORG"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "UNK"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "PERSON", "GPE"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "PERSON", "ORG"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "PERSON", "UNK"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "GPE", "PERSON"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "GPE", "ORG"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "GPE", "UNK"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "ORG", "GPE"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "ORG", "PERSON"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "ORG", "UNK"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "UNK", "GPE"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "UNK", "PERSON"));
        nerTypeFeatures.add(new StringValueMatch(EntityAttribute.ENTITY_TYPE, "UNK", "ORG"));
        
//        
//        //Topic Model/Distributed Features
        List<EntityFeatureExtractor> distFeatures = new ArrayList<>();
        distFeatures.add(new CosineSimilarity(EntityAttribute.CONTEXT_VECTOR));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_25));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_25));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_50));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_50));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_100));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_100));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_200));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_200));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_300));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_300));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_400));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_400));
//        distFeatures.add(new CosineSimilarity(EntityAttribute.TOPIC_500));
//        distFeatures.add(new JSDivergence(EntityAttribute.TOPIC_500));
        
        List<EntityFeatureExtractor> binnedDistFeatures = new ArrayList<>();
        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.CONTEXT_VECTOR), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_25), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_25), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_50), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_50), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_100), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_100), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_200), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_200), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_300), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_300), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_400), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_400), 8));
//        binnedDistFeatures.add(new BinningFeature(new CosineSimilarity(EntityAttribute.TOPIC_500), 8));
//        binnedDistFeatures.add(new BinningFeature(new JSDivergence(EntityAttribute.TOPIC_500), 8));
        
        //Feature Combinations
        
//        extractors.addAll(nameFeatures);
        extractors.addAll(aliasFeatures);
        extractors.addAll(realAliasFeatures);
//        extractors.addAll(binnedNameFeatures);
        extractors.addAll(binnedAliasFeatures);
        extractors.addAll(nerFeatures);
        extractors.addAll(nerTypeFeatures);
        extractors.addAll(distFeatures);
        extractors.addAll(binnedDistFeatures);
        
        featureCombinations.add(Lists.newArrayList(nerTypeFeatures, binnedAliasFeatures));
        featureCombinations.add(Lists.newArrayList(nerTypeFeatures, aliasFeatures));
        featureCombinations.add(Lists.newArrayList(nerTypeFeatures, binnedDistFeatures));
        featureCombinations.add(Lists.newArrayList(nerTypeFeatures, binnedAliasFeatures, binnedDistFeatures));
        featureCombinations.add(Lists.newArrayList(nerTypeFeatures, aliasFeatures, binnedDistFeatures));
        return new EntityPairInstanceConverter(extractors, featureCombinations);
    }
    
    private FeatureCombination createCombination(List<Collection<EntityFeatureExtractor>> extractors, FeatureIndexer indexer){
        if(extractors.size() > 3 || extractors.size() <= 1) {
            throw new RuntimeException("Cannot create combination with less than 2 or more than 3 features");
        }
        if(extractors.size() == 2) {
            Collection<FeatureDescriptor> ex0 = extractors.get(0).stream().flatMap(e -> e.featureNames().stream()).collect(Collectors.toList());
            Collection<FeatureDescriptor> ex1 = extractors.get(1).stream().flatMap(e -> e.featureNames().stream()).collect(Collectors.toList());
            return new FeatureCombination(ex0, ex1, indexer);
        } else {
            Collection<FeatureDescriptor> ex0 = extractors.get(0).stream().flatMap(e -> e.featureNames().stream()).collect(Collectors.toList());
            Collection<FeatureDescriptor> ex1 = extractors.get(1).stream().flatMap(e -> e.featureNames().stream()).collect(Collectors.toList());
            Collection<FeatureDescriptor> ex2 = extractors.get(2).stream().flatMap(e -> e.featureNames().stream()).collect(Collectors.toList());
            return new FeatureCombination(ex0, ex1, ex2, indexer);
        }
    }
    
    public EntityPairInstanceConverter(List<EntityFeatureExtractor> extractors, List<List<Collection<EntityFeatureExtractor>>> featureCombinations) {
        this.extractors = extractors;
        this.featureCombinations = new ArrayList<>();
        
        this.indexer = new FeatureIndexer();
        this.indexer.getOrAdd(FeatureDescriptor.of("***dummy***"));
        this.indexer.getOrAdd(FeatureDescriptor.of("isNil"));
        for(EntityFeatureExtractor extractor : extractors) { 
            for(FeatureDescriptor desc : extractor.featureNames()) {
                this.indexer.getOrAdd(desc);
            }
        }
        for(List<Collection<EntityFeatureExtractor>> combo : featureCombinations) {
            FeatureCombination featureCombination = createCombination(combo, this.indexer);
            this.featureCombinations.add(featureCombination);
            for(FeatureDescriptor desc : featureCombination.featureNames()) {
                this.indexer.getOrAdd(desc);
            }
        }
    }
    
    public Map<Integer, Double> convert(KBEntity query, KBEntity candidate) {
        Map<Integer, Double> instance = new HashMap<>();
        for(EntityFeatureExtractor extractor : extractors) {
            Map<FeatureDescriptor, Double> features = extractor.extractFeatures(query, candidate);
            for(FeatureDescriptor key : features.keySet()) {
                if(features.get(key) == 0) { 
                    continue;
                }
                int index = indexer.getIndex(key);
                instance.put(index, features.get(key));
                
            }
        }
        for(FeatureCombination combo : featureCombinations) {
            Map<FeatureDescriptor, Double> features = combo.extractFeatures(instance);
            for(FeatureDescriptor key : features.keySet()) {
                if(features.get(key) == 0.0) {
                    continue;
                }
                int index = indexer.getIndex(key);
                instance.put(index, features.get(key));
            }
        }
        return instance;
    }
    
    public Map<Integer, Double> createNil(){ 
        Map<Integer, Double> instance = new HashMap<>();
        for(Map.Entry<FeatureDescriptor, Integer> feat : indexer) {
            if(feat.getValue() == 0) {
                continue;
            } else if(feat.getValue() == 1) {
                instance.put(feat.getValue(), 1.0);
            } else {
                instance.put(feat.getValue(), 0.0);
            }
        }
        return instance;
    }
    
    public void writeFeatures(String featFile) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(featFile)))){
            for(int i = 0; i < indexer.size(); i++) {
                writer.write(i + ": " + indexer.getFeature(i).getName() + "\n");
            }
        } catch (IOException e) {
            LOG.error("Unable to write features to file " + featFile, e);
        }
    }
    
    
}
