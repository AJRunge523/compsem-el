package com.arunge.el.processing;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.arunge.el.api.EntityStore;
import com.arunge.el.api.KBEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;

public class EntityLinkingPipeline {

    public static void main(String[] args) {
        String queryString = "Baltimore City";
        KBEntity query = new KBEntity();
        query.setCanonicalName(queryString);
        Set<String> words = new HashSet<>();
        words.add("Baltimore");
        words.add("City");
        query.setNameUnigrams(words);
        EntityStore entityStore = new MongoEntityStore(new MongoClient("localhost", 27017), "entity_store");
        EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(entityStore);
        Stream<KBEntity> entities = candidateRetrieval.retrieveCandidates(query);
        ScoredEntity best = entities.map(e -> scoreEntity(query, e)).sorted().findFirst().get();
        System.out.println(best.getEntity().getId() + " --> " + best.getEntity().getCanonicalName());
    }
    
    private static ScoredEntity scoreEntity(KBEntity query, KBEntity kbEntry) {
        int score = Sets.intersection(query.getNameUnigrams(), kbEntry.getNameUnigrams()).size();
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
