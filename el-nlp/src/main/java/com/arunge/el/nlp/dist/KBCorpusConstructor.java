package com.arunge.el.nlp.dist;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ContextType;
import com.arunge.el.kb.text.MongoKBDocTextSource;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.corpus.Corpus;
import com.arunge.nlp.corpus.CorpusBuilder;
import com.arunge.nlp.corpus.CorpusDocument;
import com.arunge.nlp.corpus.TFType;
import com.arunge.nlp.stanford.Tokenizers;
import com.mongodb.MongoClient;

public class KBCorpusConstructor {

    private static String CORPUS_TYPE = "TFIDF";
    
    private static TFType tfType = TFType.LENGTH_NORM;
    
    private static Logger LOG = LoggerFactory.getLogger(KBCorpusConstructor.class);
    
    public static void main(String[] args) throws IOException { 
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "entity_store");
        switch(CORPUS_TYPE) { 
        case "COUNT":
            buildCountCorpus(client, store);
            break;
        case "TFIDF":
            buildTfIdfCorpus(client, store, tfType);
            break;
        default:
            LOG.error("Unknown corpus type: {}, exiting.", CORPUS_TYPE);
            System.exit(1);
        }

    }
    
    private static void buildCountCorpus(MongoClient client, MongoEntityStore store) throws IOException { 
        LOG.info("Building count corpus");
        Corpus corpus = CorpusBuilder.countingVocabCorpusBuilder()
                .addSource(new MongoKBDocTextSource(store))
                .withTokenizer(Tokenizers.getDefaultFiltered())
                .build();
        
//        CountingCorpus corpus = (CountingCorpus) CountingCorpus.loadCorpus(new File("output/kb-count-corpus.corpus"));
        
        LOG.info("Finished loading corpus, exporting vectors.");
        corpus.export("output/", "kb-count-corpus");
        LOG.info("Finished exporting corpus, writing vectors to DB.");
        int numProcessed = 0;
        for(CorpusDocument doc : corpus) {
            store.updateNLPDocument(doc.getDocId(), ContextType.COUNT.name(), doc.getVocab());
            CorpusDocument lengthNormDoc = doc.buildLengthNormCountDoc();
            store.updateNLPDocument(doc.getDocId(), ContextType.NORM_COUNT.name(), lengthNormDoc.getVocab());
            CorpusDocument logLengthNormDoc = doc.buildLogLengthNormCountDoc();
            store.updateNLPDocument(doc.getDocId(), ContextType.LOG_NORM_COUNT.name(), logLengthNormDoc.getVocab());
            numProcessed += 1;
            if(numProcessed % 1000 == 0) { 
                LOG.info("Processed {} documents.", numProcessed);
            }
        }
    }
    
    private static void buildTfIdfCorpus(MongoClient client, MongoEntityStore store, TFType type) throws IOException { 
        LOG.info("Building TF-IDF corpus");
        Corpus corpus = CorpusBuilder.tfIdfCorpusBuilder(type)
                .addSource(new MongoKBDocTextSource(store))
                .withTokenizer(Tokenizers.getDefaultFiltered())
                .withMinVocabInclusion(0, 5)
                .build();
        
//        CountingCorpus corpus = (CountingCorpus) CountingCorpus.loadCorpus(new File("output/kb-count-corpus.corpus"));
        
        LOG.info("Finished loading corpus, exporting vectors.");
        corpus.export("output/", String.format("kb-tfidf-%s-corpus", type.name().toLowerCase()));
        LOG.info("Finished exporting corpus, writing vectors to DB.");
//        int numProcessed = 0;
//        for(CorpusDocument doc : corpus) {
//            switch(type) {
//            case LENGTH_NORM:
//                store.updateNLPDocument(doc.getDocId(), ContextType.NORM_TFIDF.name(), doc.getVocab());
//                break;
//            case LOG_LENGTH_NORM:
//                store.updateNLPDocument(doc.getDocId(), ContextType.LOG_NORM_TFIDF.name(), doc.getVocab());
//                break;
//            default:
//                break;
//            }
//            numProcessed += 1;
//            if(numProcessed % 1000 == 0) { 
//                LOG.info("Processed {} documents.", numProcessed);
//            }
//        }
    }
    
}
