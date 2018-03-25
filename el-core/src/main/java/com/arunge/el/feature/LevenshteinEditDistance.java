package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

/**
 * 
 *<p>Computes the normalized Levenshtein distance between two strings.<p>
 *
 * @author Andrew Runge
 *
 */
public class LevenshteinEditDistance extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    
    public LevenshteinEditDistance(EntityAttribute queryAndCandidateAttribute) {
        super(queryAndCandidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAndCandidateAttribute.name() + "_edit");
    }

    public LevenshteinEditDistance(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        super(queryAttribute, candidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAttribute.name() + "+" + candidateAttribute.name() + "_edit");
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        StringAttribute first = (StringAttribute) query;
        StringAttribute second = (StringAttribute) candidate;
        double editDistance = (double) computeNormalizedEditDistance(first.getValueAsStr(), second.getValueAsStr());
        features.put(featureName, editDistance);
        return features;
    }
    
    private static double computeNormalizedEditDistance(String s1, String s2) {
        double norm = s1.length() > s2.length() ? s1.length() : s2.length();
        int[] prevRow = new int[s1.length() + 1];
        int prevInCol = -1;
        for(int i = 0; i < prevRow.length; i++) {
            prevRow[i] = i;
        }
        
        for(int i = 1; i < s2.length() + 1; i++) {
            prevInCol = i;
            int[] nextRow = new int[s1.length() + 1];
            nextRow[0] = prevInCol;
            for(int j = 1; j < s1.length() + 1; j++) {
                if(s2.charAt(i - 1) == s1.charAt(j - 1)) {
                    nextRow[j] = prevRow[j - 1];
                } else {
                    int deletionCost = prevInCol + 1;
                    int replaceCost = prevRow[j] + 1;
                    int substCost = prevRow[j - 1] + 1;
                    int cost = Math.min(Math.min(deletionCost, replaceCost), substCost);
                    nextRow[j] = cost;
                }
                prevInCol = nextRow[j];
            }
            prevRow = nextRow;
        }
        return prevRow[prevRow.length - 1] / norm;
    }
    
    public static void main(String[] args) {
        System.out.println(LevenshteinEditDistance.computeNormalizedEditDistance("bugs", "bags"));
    }
    
    
}
