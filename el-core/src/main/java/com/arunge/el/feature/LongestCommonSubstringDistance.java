package com.arunge.el.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.similarity.LongestCommonSubsequenceDistance;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.attribute.Attribute;
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
        // TODO Auto-generated constructor stub
    }
    
    public LongestCommonSubstringDistance(EntityAttribute queryAttribute, EntityAttribute candidateAttribute) {
        super(queryAttribute, candidateAttribute);
        this.featureName = FeatureDescriptor.of(queryAttribute.name() + "+" + candidateAttribute.name() + "_lcs");

        // TODO Auto-generated constructor stub
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
        double longest = lcs.apply(first.getValueAsStr(), second.getValueAsStr());
        if(first.getValueAsStr().length() > second.getValueAsStr().length()) {
            longest = longest / first.getValueAsStr().length();
        } else {
            longest = longest / second.getValueAsStr().length();
        }
        features.put(featureName, longest);
        return features;
    }
    
    public static void main(String[] args) { 
        LongestCommonSubstringDistance lcs = new LongestCommonSubstringDistance(EntityAttribute.ACRONYM);
        System.out.println(lcs.extract(StringAttribute.valueOf("harry potter"), StringAttribute.valueOf("hasty porter")).get(lcs.featureName));
    }

}
