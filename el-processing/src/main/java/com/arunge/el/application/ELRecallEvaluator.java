package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import com.mongodb.MongoClient;

public class ELRecallEvaluator {

    public static void main(String[] args) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output/recall-misses.txt")))) {
            EntityKBStore entityStore = new MongoEntityStore(new MongoClient("localhost", 27017), "entity_store");
            KBDocumentTextProcessor textProcessor = new KBDocumentTextProcessor();
            KBEntityConverter entityConverter = new KBEntityConverter();
            EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(entityStore);

            int numGoldFound = 0;
            int totalQueries = 0;
            int minCandidates = Integer.MAX_VALUE;
            int maxCandidates = Integer.MIN_VALUE;
            int totalCandidates = 0;
            Iterable<ELQuery> queries = QuerySetLoader.loadTAC2010Train();
            for (ELQuery query : queries) {
                TextEntity textEntity = query.convertToEntity();
                NLPDocument nlp = textProcessor.process(textEntity);
                KBEntity queryEntity = entityConverter.convert(textEntity, nlp);

                String goldId = queryEntity.getMeta("gold").get();
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
                Optional<KBEntity> matchEntity = candidates.stream().filter(e -> e.getId().equals(goldId)).findFirst();
                if (matchEntity.isPresent()) {
                    numGoldFound += 1;
                } else {
                    writer.write(query.getQueryId() + "\t" + query.getName() + "\t" + goldId + "\n");
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
