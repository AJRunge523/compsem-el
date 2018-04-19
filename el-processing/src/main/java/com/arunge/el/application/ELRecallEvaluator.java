package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class ELRecallEvaluator {

    private static Logger LOG = LoggerFactory.getLogger(ELRecallEvaluator.class);
    
    public static void main(String[] args) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output/recall-misses-eval.txt")))) {
            MongoClient client = new MongoClient("localhost", 27017);
            EntityKBStore kbStore = MongoEntityStore.kbStore(client);
            EntityKBStore queryStore = MongoEntityStore.evalStore(client);
            EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(kbStore);

            int numGoldFound = 0;
            int totalQueries = 0;
            int minCandidates = Integer.MAX_VALUE;
            int maxCandidates = Integer.MIN_VALUE;
            int totalCandidates = 0;
            CloseableIterator<KBEntity> trainingQueries = queryStore.allEntities();
            
            while(trainingQueries.hasNext()) {
                KBEntity queryEntity = trainingQueries.next();

                String goldId = queryEntity.getAttribute(EntityAttribute.GOLD_LABEL).getValueAsStr();
                if (goldId.equals("NIL")) {
                    continue;
                }
                List<KBEntity> candidates = candidateRetrieval.retrieveCandidates(queryEntity)
                        .collect(Collectors.toList());
                int numCandidates = candidates.size();
                if (numCandidates < minCandidates) {
                    minCandidates = numCandidates;
                }
                if (numCandidates > maxCandidates) {
                    maxCandidates = numCandidates;
                }
                totalCandidates += numCandidates;
                LOG.info("Retrieved {} candidates for query {} with name {}", numCandidates, queryEntity.getId(), queryEntity.getName());
                Optional<KBEntity> matchEntity = candidates.stream().filter(e -> e.getId().equals(goldId)).findFirst();
                if (matchEntity.isPresent()) {
                    numGoldFound += 1;
                } else {
                    writer.write(queryEntity.getId() + "\t" + queryEntity.getName() + "\t" + goldId + "\n");
                }
                totalQueries += 1;

            }
            double recall = ((double) numGoldFound) / totalQueries;
            writer.write("Number of gold entities found: " + numGoldFound + ", total: " + totalQueries + "\n");
            writer.write("Recall: " + recall + "\n");
            writer.write(String.format("Minimum candidates: %d, Maximum candidates: %d, Average candidates: %f",
                    minCandidates, maxCandidates, ((float) totalCandidates) / totalQueries));
        }
    }

}
