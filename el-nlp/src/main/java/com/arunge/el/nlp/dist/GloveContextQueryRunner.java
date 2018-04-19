package com.arunge.el.nlp.dist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import com.arunge.nlp.stanford.FilteredTokenizer;
import com.arunge.nlp.stanford.StanfordTokenizer;
import com.arunge.nlp.tokenization.TokenFilters;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;
import com.arunge.nlp.vocab.CountingVocabulary;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class GloveContextQueryRunner {

    private static int WINDOW_SIZE = 100;
    
    private static Logger LOG = LoggerFactory.getLogger(GloveContextRunner.class);
    
    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        CloseableIterator<TextEntity> textEntities = store.allKBText();
        List<TokenFilter> filters = new ArrayList<>();
        filters.add(TokenFilters.ascii());
        filters.add(TokenFilters.numberFilter());
        filters.add(TokenFilters.punctuation());
        filters.add(TokenFilters.stopwords());
        Tokenizer tokenizer = new FilteredTokenizer(new StanfordTokenizer(), filters);
        MapDBGloveStore glove = new MapDBGloveStore("glove.db");
        CountingVocabulary indexer = CountingVocabulary.read(new File("output/kb-tfidf-length_norm-corpus.vocab"));
        int numProcessed = 0;
        while(textEntities.hasNext()) {
            TextEntity e = textEntities.next();
            String content = e.getDocText();
            String slightlyNormContent = content.replaceAll("\n", " ");
            int mentionIndex = slightlyNormContent.indexOf(e.getName());
            System.out.println(e.getName() + ", " + mentionIndex);
            List<Token> tokens = tokenizer.tokenizeToList(content);
            List<Token> windowTokens = getWindow(mentionIndex, mentionIndex + e.getName().length(), tokens, WINDOW_SIZE);
            Map<ContextType, double[]> vectors = createContextVectors(glove, tokens, windowTokens, indexer);
            for(ContextType type : vectors.keySet()) { 
                store.updateNLPDocument(e.getId(), type.name(), vectors.get(type));
            }
            numProcessed += 1;
            if(numProcessed % 10000 == 0) { 
                LOG.info("Processed {} documents.", numProcessed);
            }
//            break;
        }
        client.close();
    }
    
    private static List<Token> getWindow(int startIndex, int endIndex, List<Token> tokens, int windowSize) {
        int startTok = -1;
        int endTok = -1;
        for(int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if(t.start() == startIndex && startTok == -1) {
                startTok = i;
            }
            else if(t.start() > startIndex && startTok == -1) {
                startTok = i-1;
            }
            if(t.end() == endIndex && endTok == -1) { 
                endTok = i;
                break;
            } else if(t.start() > endIndex && endTok == -1) {
                endTok = i-1;
                break;
            }
        }
        if(startTok == -1 || endTok == -1) {
            if(endTok != -1) {
                startTok = endTok - 1;
            } else {
                throw new RuntimeException("AAAAH" + startTok + ", " + endTok);
            }
        }
        List<Token> windowTokens = new ArrayList<>();
        int lowerBound = startTok - (windowSize / 2);
        if(lowerBound < 0) {
            lowerBound = 0;
        }
        int upperBound = endTok + (windowSize / 2);
        if(upperBound > tokens.size()) {
            upperBound = tokens.size();
        }
        for(int i = lowerBound; i < startTok; i++) {
            windowTokens.add(tokens.get(i));
        }
        for(int i = endTok + 1; i < upperBound; i++) {
            windowTokens.add(tokens.get(i));
        }
        return windowTokens;
    }
    
    private static Map<ContextType, double[]> createContextVectors(GloveStore glove, List<Token> tokens, List<Token> windowTokens, CountingVocabulary indexer) {
        
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
            }
        }      
        for(int i = 0; i < windowTokens.size(); i++) {
            String word = windowTokens.get(i).text().toLowerCase();
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
                windowContext[dim] += vec[dim] / windowTokens.size();
                windowTfidf[dim] += vec[dim] * idfWeight * 1.0 / windowTokens.size();
            }
        }
        
        Map<ContextType, double[]> contextVectors = new HashMap<>();
        contextVectors.put(ContextType.FULL_EMB, fullContext);
        contextVectors.put(ContextType.WINDOW_EMB, windowContext);
        contextVectors.put(ContextType.FULL_EMB_TFIDF, fullContext);
        contextVectors.put(ContextType.WINDOW_EMB_TFIDF, windowContext);
        return contextVectors;
    }
    
}
