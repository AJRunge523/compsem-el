package com.arunge.el.nlp.dist;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.vocab.CountingVocabulary;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

public class GloveContextRunner {

    private static int WINDOW_SIZE = 100;
    
    private static Logger LOG = LoggerFactory.getLogger(GloveContextRunner.class);
    
    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.kbStore(client);
        Tokenizer tokenizer = Tokenizers.getDefaultFiltered();
        MapDBGloveStore glove = new MapDBGloveStore("J:/glove/glove.db");
        CountingVocabulary indexer = CountingVocabulary.read(new File("output/kb-tfidf-length_norm-corpus.vocab"));
        ContextStore contexts = ContextStore.getDefault();
        int numProcessed = 0;
        List<String> ids = Files.readLines(new File("src/main/resources/candIds.txt"), Charsets.UTF_8);
        for(String id : ids) {
            TextEntity e = store.fetchKBText(id).get();
            String content = e.getDocText();
            List<Token> tokens = tokenizer.tokenizeToList(content);
            Map<ContextType, double[]> vectors = createContextVectors(glove, tokens, WINDOW_SIZE, indexer);
            for(ContextType type : vectors.keySet()) { 
                contexts.putContext(type, e.getId(), vectors.get(type));
            }
            numProcessed += 1;
            if(numProcessed % 1000 == 0) { 
                LOG.info("Processed {} documents.", numProcessed);
            }
        }
        contexts.close();
    }
    
    private static Map<ContextType, double[]> createContextVectors(GloveStore glove, List<Token> tokens, int windowSize, CountingVocabulary indexer) {
        
        double[] fullContext = new double[300];
        double[] windowContext = new double[300];
        double[] fullTfidf = new double[300];
        double[] windowTfidf = new double[300];
        double[] idfWeights = indexer.computeIDFVector();
        for(int i = 0; i < tokens.size(); i++) {
            String word = tokens.get(i).text().toLowerCase();
            float[] vec = glove.getWord(word);
            if(vec == null) {
//                LOG.warn("No vector for word {}", word);
                continue;
            }
            int wordIndex = indexer.getIndex(word);
            double idfWeight = 0.0;
            if(wordIndex != -1) {
                idfWeight = idfWeights[wordIndex];
            }
            for(int dim = 0; dim < vec.length; dim++) {
                fullContext[dim] += vec[dim] / tokens.size();
                //Compute the length-norm tf-idf weight for this instance of the word.
                fullTfidf[dim] += vec[dim] * idfWeight * 1.0 / tokens.size();
                if(i < windowSize) {
                    windowContext[dim] += vec[dim] / windowSize;
                    windowTfidf[dim] += vec[dim] * idfWeight * 1.0 / windowSize;
//                    System.out.println(idfWeight + ", " + windowContext[dim] + ", " + windowTfidf[dim]);
                }
                
            }
        }        
        Map<ContextType, double[]> contextVectors = new HashMap<>();
        contextVectors.put(ContextType.FULL_EMB, fullContext);
        contextVectors.put(ContextType.WINDOW_EMB, windowContext);
        contextVectors.put(ContextType.FULL_EMB_TFIDF, fullTfidf);
        contextVectors.put(ContextType.WINDOW_EMB_TFIDF, windowTfidf);
        return contextVectors;
    }
}
