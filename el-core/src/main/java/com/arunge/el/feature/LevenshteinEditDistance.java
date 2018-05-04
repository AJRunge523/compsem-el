package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
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
        double editDistance = 1.0;
        if(query instanceof StringAttribute && candidate instanceof StringAttribute) { 
            StringAttribute first = (StringAttribute) query;
            StringAttribute second = (StringAttribute) candidate;
            editDistance = computeNormalizedEditDistance(first.getValueAsStr(), second.getValueAsStr());
        } else if(query instanceof SetAttribute && candidate instanceof StringAttribute) {
            editDistance = computeStringDistance((StringAttribute) candidate, (SetAttribute) query);
        } else if(query instanceof StringAttribute && candidate instanceof SetAttribute) {
            editDistance = computeStringDistance((StringAttribute) query, (SetAttribute) candidate);
        } else if(query instanceof SetAttribute && candidate instanceof SetAttribute) {
            editDistance = computeStringDistance((SetAttribute) query, (SetAttribute) candidate);
        }

        features.put(featureName, editDistance);
        return features;
    }
    
    private double computeStringDistance(StringAttribute first, SetAttribute second) {
        if(second.getSetValue() == null || second.getSetValue().isEmpty()) {
            return 0.5;
        }
        double best = 1.0;
        for(String s : second.getSetValue()) {
            double distance = computeNormalizedEditDistance(first.getValueAsStr(), s);
            if(distance < best) {
                best = distance;
            }
        }
        return best;
    }
    
    private double computeStringDistance(SetAttribute first, SetAttribute second) {
        double best = 1.0;
        if(first.getSetValue() == null || first.getSetValue().isEmpty() || 
                second.getSetValue() == null || second.getSetValue().isEmpty()) {
            return 0.0;
        }
        for(String s : second.getSetValue()) {
            double distance = computeNormalizedEditDistance(first.getValueAsStr(), s);
            if(distance < best) {
                best = distance;
            }
        }
        return best;
    }
    
    public static double computeNormalizedEditDistance(String s1, String s2) {
        if(s1.length() == 0 && s2.length() == 0) { 
            return 0.0;
        }
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
        System.out.println(LevenshteinEditDistance.computeNormalizedEditDistance("", ""));
    }
    
    
}
