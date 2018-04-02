package com.arunge.el.nlp.dist;

import java.io.File;
import java.io.IOException;

import com.arunge.el.api.ContextType;
import com.arunge.el.kb.text.MongoKBDocTextSource;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.api.Corpus;
import com.arunge.nlp.api.CorpusDocument;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.vocab.CorpusBuilder;
import com.arunge.nlp.vocab.CountingVocabulary;
import com.mongodb.MongoClient;

public class ELQueryContextVectorBuilder {

    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "el_training_query_store");
        CountingVocabulary vocabulary = CountingVocabulary.read(new File("output/kb-tfidf-length_norm-corpus.vocab"));
        Corpus corpus = CorpusBuilder.fixedTfIdfCorpusBuilder(vocabulary)
                .addSource(new MongoKBDocTextSource(store))
                .withTokenizer(Tokenizers.getDefaultFiltered())
                .build();
        for(CorpusDocument d : corpus) {
            store.updateNLPDocument(d.getDocId(), ContextType.NORM_TFIDF.name(), d.getVocab());
        }
        
    }
    
}
