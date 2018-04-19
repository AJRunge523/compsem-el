package com.arunge.el.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.nlp.api.FeatureDescriptor;

public class BinningFeature extends EntityFeatureExtractor {

    private EntityFeatureExtractor extractor;
    private float start;
    private float end;
    private float increment;
    
    /**
     * Creates a BinningFeatureExtrator that splits the features from the provided extractor into 4 bins between 0.0 and 1.0.
     * @param extractor
     */
    public BinningFeature(EntityFeatureExtractor extractor) {
        this(extractor, 0.0f, 1.0f, 4);
    }
    
    /**
     * Creates a BinningFeatureExtrator that splits the features from the provided extractor into numBins bins between 0.0 and 1.0.
     * @param extractor
     */
    public BinningFeature(EntityFeatureExtractor extractor, int numBins) {
        this(extractor, 0.0f, 1.0f, numBins);
    }
    
    /**
     * Creates a BinningFeatureExtrator that splits the features from the provided extractor into numBins bins between (start, end)
     * @param extractor
     */
    public BinningFeature(EntityFeatureExtractor extractor, float start, float end, int numBins) {
        super();
        this.extractor = extractor;
        this.start = start;
        this.end = end;
        this.increment = ((float) (end - start)) / numBins;
    }
    
    @Override
    public Set<FeatureDescriptor> featureNames() {
        Set<FeatureDescriptor> feats = extractor.featureNames();
        Set<FeatureDescriptor> binnedFeats = new HashSet<>();
        for(FeatureDescriptor feat : feats) {
            for(float f = start + increment; f <= end; f+= increment) {
                binnedFeats.add(FeatureDescriptor.of(feat.getName() + "_<=" + f));
            }
            binnedFeats.add(FeatureDescriptor.of(feat.getName() + "_<" + start));
            binnedFeats.add(FeatureDescriptor.of(feat.getName() + ">=" + end));
        }
        return binnedFeats;
    }

    @Override
    public Map<FeatureDescriptor, Double> extractFeatures(KBEntity e1, KBEntity e2) {
        Map<FeatureDescriptor, Double> features = new HashMap<>();
        Map<FeatureDescriptor, Double> origFeatures = extractor.extractFeatures(e1, e2);
        for(FeatureDescriptor feat : origFeatures.keySet()) {
            double val = origFeatures.get(feat);
            for(float f = start + increment; f <= end; f+= increment) { 
                FeatureDescriptor binned = FeatureDescriptor.of(feat.getName() + "_<=" + f);
                if(val >= (f - increment) && val < f) {
                    features.put(binned, 1.0);
                } else {
                    features.put(binned, 0.0);
                }
            }
            FeatureDescriptor startBin = FeatureDescriptor.of(feat.getName() + "_<" + start);
            FeatureDescriptor endBin = FeatureDescriptor.of(feat.getName() + ">=" + end);

            if(val < start) {
                features.put(startBin, 1.0);
            } else {
                features.put(startBin, 0.0);
            }
            if(val >= end) {
                features.put(endBin, 1.0);
            } else {
                features.put(endBin, 0.0);
            }

        }
        return features;
    }
    
    @Override
    protected Map<FeatureDescriptor, Double> extract(Attribute query, Attribute candidate) {
        throw new UnsupportedOperationException("No feature extraction is done on attributes with this extractor");
    }

}
