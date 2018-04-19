package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.FeatureDescriptor;
import com.google.common.collect.Sets;

public class LongestCommonSubstringDistance extends EntityFeatureExtractor {

    private FeatureDescriptor featureName;
    private LongestCommonSubsequenceDistance lcs;
    
    public LongestCommonSubstringDistance(EntityAttribute queryAndCandidateAttribute) {
        super(queryAndCandidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAndCandidateAttribute.name() + "_lcs");
        this.lcs = new LongestCommonSubsequenceDistance();
    }
    
    public LongestCommonSubstringDistance(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        super(queryAttribute, candidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAttribute.name() + "+" + candidateAttribute.name() + "_lcs");
        this.lcs = new LongestCommonSubsequenceDistance();
    }

    @Override
    public Set<FeatureDescriptor> featureNames() {
        return Sets.newHashSet(featureName);
    }

    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        double dist = 1.0;
        if(query instanceof StringAttribute && candidate instanceof StringAttribute) { 
            StringAttribute first = (StringAttribute) query;
            StringAttribute second = (StringAttribute) candidate;
            dist = computeDistance(first.getValueAsStr(), second.getValueAsStr());
        } else if(query instanceof SetAttribute && candidate instanceof StringAttribute) {
            dist = computeStringDistance((StringAttribute) candidate, (SetAttribute) query);
        } else if(query instanceof StringAttribute && candidate instanceof SetAttribute) {
            dist = computeStringDistance((StringAttribute) query, (SetAttribute) candidate);
        } else if(query instanceof SetAttribute && candidate instanceof SetAttribute) { 
            dist = computeStringDistance((SetAttribute) query, (SetAttribute) candidate);
        }
        features.put(featureName, dist);
        return features;
    }
    
    private double computeStringDistance(StringAttribute first, SetAttribute second) {
        double best = 1.0;
        String s1 = first.getValueAsStr();
        for(String s2 : second.getSetValue()) {
            double dist = computeDistance(s1, s2);
            if(dist < best) {
                best = dist;
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
        for(String s1 : first.getSetValue()) {
            for(String s2 : second.getSetValue()) {
                double dist = computeDistance(s1, s2);
                if(dist < best) {
                    best = dist;
                }
            }
        }
        return best;
    }
    
    private double computeDistance(String s1, String s2) {
        if(s1.length() == 0 && s2.length() == 0) { 
            return 0.0;
        }
        double longest = lcs.apply(s1, s2);
        if(s1.length() > s2.length()) {
            longest = longest / (s2.length() + s1.length());
        } else {
            longest = longest / (s1.length() + s2.length());
        }
        return longest;
    }
    
    public static void main(String[] args) { 
        LongestCommonSubstringDistance lcs = new LongestCommonSubstringDistance(EntityAttribute.ACRONYM);
        System.out.println(lcs.extract(StringAttribute.valueOf("harry potter"), StringAttribute.valueOf("harry potter")).get(lcs.featureName));
    }

}
