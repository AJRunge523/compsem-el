package com.arunge.el.kb.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.ingest.TextSource;
import com.arunge.nlp.api.FeatureDescriptor;
import com.arunge.nlp.api.FeatureExtractor;
import com.arunge.nlp.text.FeatureTextDocument;
import com.arunge.nlp.text.TextDocument;
import com.arunge.unmei.iterators.Iterators;

public class MongoKBDocTextSource implements TextSource {

    private MongoEntityStore store;
    private List<FeatureExtractor<TextEntity>> featureExtractors;
    
    public MongoKBDocTextSource(MongoEntityStore store) {
        this.store = store;
        this.featureExtractors = new ArrayList<>();
    }

    public MongoKBDocTextSource withFeature(FeatureExtractor<TextEntity> extractor) {
        this.featureExtractors.add(extractor);
        return this;
    }
    
    public MongoKBDocTextSource withFeature(List<FeatureExtractor<TextEntity>> extractors) {
        this.featureExtractors.addAll(extractors);
        return this;
    }
    
    @Override
    public Stream<TextDocument> getDocuments() {
        return Iterators.toStream(store.allKBText()).map(kbEntry -> convert(kbEntry));
    }
    
    private TextDocument convert(TextEntity entity) {
        FeatureTextDocument textDoc = new FeatureTextDocument(entity.getId(), entity.getDocText());
        for(FeatureExtractor<TextEntity> extractor : featureExtractors) {
            Map<FeatureDescriptor, Double> features = extractor.extractFeatures(entity);
            for(FeatureDescriptor key : features.keySet()) {
                textDoc.addFeature(key, features.get(key));
            }
        }
        return textDoc;
    }

}
