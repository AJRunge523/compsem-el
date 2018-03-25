package com.arunge.el.nlp.dist;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.vocab.CountingVocabulary;
import com.mongodb.MongoClient;

public class KBCorpusVocabTest {

    public static void main(String[] args) throws IOException {
        CountingVocabulary vocab = CountingVocabulary.read(new File("output/kb-count-corpus.vocab"));
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "entity_store");
        NLPDocument doc = store.fetchNLPDocument("E0000005").get();
        Map<Integer, Double> dist = doc.getDistribution(ContextType.COUNT);
        for(Integer key : dist.keySet()) {
            System.out.println(key + " --> " + vocab.getWord(key) + ": " + dist.get(key));
        }
    }
    
}
