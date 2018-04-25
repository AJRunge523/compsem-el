package com.arunge.el.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arunge.nlp.api.AnnotatedToken;

public class NLPDocument {

    private String id;
    
    private EntityType entityType;
    
    /* List of sentences containing annotated tokens */
    private List<List<AnnotatedToken>> tokens;
    /* Map containing various distributional representations of the document */
    private Map<ContextType, Map<Integer, Double>> distributions;
    
    private Set<String> aliases;
    
    /* Entities that refer to this entity in their own text */
    private Set<String> inrefEntities;
    
    /* Entities that this entity refers to in its own text. */
    private Set<String> outrefEntities;

    /* Entities that refer to each other */
    private Set<String> corefEntities;
    
    private double inLinks;
    private double outLinks;
    
    public NLPDocument(String id) {
        this.id = id;
        this.tokens = new ArrayList<>();
        this.distributions = new HashMap<>();
        this.aliases = new HashSet<>();
        this.inrefEntities = new HashSet<>();
        this.outrefEntities = new HashSet<>();
        this.corefEntities = new HashSet<>();
        this.entityType = EntityType.UNK;
        this.inLinks = 0.0;
        this.outLinks = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<AnnotatedToken>> getTokens() {
        return tokens;
    }

    public void setTokens(List<List<AnnotatedToken>> tokens) {
        this.tokens = tokens;
    }

    public Map<ContextType, Map<Integer, Double>> getDistributions() {
        return distributions;
    }
    
    public Map<Integer, Double> getDistribution(ContextType contextType) {
        return distributions.get(contextType);
    }

    public void setDistributions(Map<ContextType, Map<Integer, Double>> distributions) {
        this.distributions = distributions;
    }

    public void addDistribution(ContextType type, Map<Integer, Double> distribution) {
        this.distributions.put(type, distribution);
    }
    
    public void setAliases(Collection<String> aliases) {
        this.aliases = new HashSet<>(aliases);
    }
    
    public void addAlias(String alias) {
        this.aliases.add(alias);
    }
    
    public Set<String> getAliases() { 
        return aliases;
    }
    public Set<String> getInrefEntities() {
        return inrefEntities;
    }

    public void setInrefEntities(Collection<String> inrefEntities) {
        this.inrefEntities = new HashSet<>(inrefEntities);
    }
    
    public Set<String> getOutrefEntities() {
        return outrefEntities;
    }

    public void setOutrefEntities(Collection<String> outrefEntities) {
        this.outrefEntities = new HashSet<>(outrefEntities);
    }

    public Set<String> getCorefEntities() {
        return corefEntities;
    }

    public void setCorefEntities(Collection<String> corefEntities) {
        this.corefEntities = new HashSet<>(corefEntities);
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public double getInLinks() {
        return inLinks;
    }

    public void setInLinks(double inLinks) {
        this.inLinks = inLinks;
    }

    public double getOutLinks() {
        return outLinks;
    }

    public void setOutLinks(double outLinks) {
        this.outLinks = outLinks;
    }
    
}
