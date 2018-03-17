package com.arunge.el.application;

import java.util.Optional;
import java.util.stream.Stream;

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.processing.KBDocumentTextProcessor;
import com.arunge.el.processing.KBEntityConverter;
import com.arunge.el.query.QuerySetLoader;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;

/**
 * 
 *<p>Main access point for processing an Entity Linking dataset.<p>
 *
 * @author Andrew Runge
 *
 */
public class EntityLinkingPipeline {

    public static void main(String[] args) {
        EntityKBStore entityStore = new MongoEntityStore(new MongoClient("localhost", 27017), "entity_store");
        KBDocumentTextProcessor textProcessor = new KBDocumentTextProcessor();
        KBEntityConverter entityConverter = new KBEntityConverter();
        EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(entityStore);
        
        int numCorrect = 0;
        int numNonNilCorrect = 0;
        int numNil = 0;
        int numNonNil = 0;
        int nilCorrect = 0;
        int total = 0;
        Iterable<ELQuery> queries = QuerySetLoader.loadTAC2010Train();
        for(ELQuery query : queries) {

            TextEntity textEntity = query.convertToEntity();
            NLPDocument nlp = textProcessor.process(textEntity);
            KBEntity queryEntity = entityConverter.convert(textEntity, nlp);
            
            Stream<KBEntity> entities = candidateRetrieval.retrieveCandidates(queryEntity);
            
            Optional<ScoredEntity> best = entities.map(e -> scoreEntity(queryEntity, e)).sorted().findFirst();
            if(best.isPresent()) {
                ScoredEntity bestEnt = best.get();
                System.out.println(queryEntity.getMeta("gold").get() + "\t" + bestEnt.getEntity().getId() + "\t" + bestEnt.getEntity().getName());
                if(queryEntity.getMeta("gold").get().equals(bestEnt.getEntity().getId())) {
                    numCorrect += 1;
                    numNonNilCorrect += 1;
                }
                numNonNil += 1;
            } else {
                if(queryEntity.getMeta("gold").get().equals("NIL")) {
                    nilCorrect += 1;
                    numCorrect += 1;
                }
                numNil += 1;
                System.out.println(queryEntity.getMeta("gold").get() + "\t" + "NIL");
            }
            total += 1;
        }
        System.out.println("# non NIL correct: " + numNonNilCorrect + " out of " + numNonNil);
        System.out.println("# NIL correct: " + nilCorrect + " out of " + numNil);
        System.out.println("Total correct: " + numCorrect + " out of " + total);
        
        
    }
    
    
    
    private static ScoredEntity scoreEntity(KBEntity query, KBEntity kbEntry) {
        int score = Sets.intersection(query.getNameUnigrams().get(), kbEntry.getNameUnigrams().get()).size();
        return new ScoredEntity(score, kbEntry);
    }
    
    private static class ScoredEntity implements Comparable<ScoredEntity> {
        private int score;
        private KBEntity entity;
        
        public ScoredEntity(int score, KBEntity entity) {
           this.score = score;
           this.entity = entity;
        }

        @Override
        public int compareTo(ScoredEntity o) {
            return score - o.score;
        }
        
        public KBEntity getEntity() {
            return entity;
        }
        
        public int getScore() {
            return score;
        }
        
    }
    
}
