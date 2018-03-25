package com.arunge.el.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arunge.nlp.api.AnnotatedToken;

public class NLPDocument {

    private String id;
        /* List of sentences containing annotated tokens */
    private List<List<AnnotatedToken>> tokens;
    /* Map containing various distributional representations of the document */
    private Map<ContextType, Map<Integer, Double>> distributions;
    
    public NLPDocument(String id) {
        this.id = id;
        this.tokens = new ArrayList<>();
        this.distributions = new HashMap<>();
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
    
}
