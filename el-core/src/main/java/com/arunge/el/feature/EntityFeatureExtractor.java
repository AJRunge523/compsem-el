package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureExtractor;

public abstract class EntityFeatureExtractor implements FeatureExtractor<Pair<KBEntity, KBEntity>> {

    protected EntityAttribute queryAttribute;
    protected EntityAttribute candidateAttribute;
    
    /**
     * Initializes a feature extractor that operates on a particular entity attribute.
     * @param attribute
     */
    public EntityFeatureExtractor(EntityAttribute queryAndCandidateAttribute) {
        this.queryAttribute = queryAndCandidateAttribute;
        this.candidateAttribute = queryAndCandidateAttribute;
    }
    
    /**
     * Initializes a feature extractor that operates on a particular entity attribute.
     * @param attribute
     */
    public EntityFeatureExtractor(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        this.queryAttribute = queryAttribute;
        this.candidateAttribute = candidateAttribute;
    }

    public Map<FeatureDescriptor, Double> extractFeatures(Pair<KBEntity, KBEntity> entityPair) {
        return extractFeatures(entityPair.getLeft(), entityPair.getRight());
    }
    
    public Map<FeatureDescriptor, Double> extractFeatures(KBEntity query, KBEntity candidate) {
        
        if(!query.hasAttribute(queryAttribute) || !candidate.hasAttribute(candidateAttribute)) {
            return new HashMap<>();
        } 
        else {
            return extract(query.getAttribute(queryAttribute), candidate.getAttribute(candidateAttribute));
        }
    }
    
    /**
     * Returns a list of the names of the features returned by this extractor.
     * @return
     */
    public abstract Set<FeatureDescriptor> featureNames();
    
    /**
     * Extracts feature values from the pair of attributes.
     * @param query
     * @param candidate
     * @return
     */
    protected abstract Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate);
    
}
