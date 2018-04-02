package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.EntityMetadataKeys;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.TextEntity;
import com.arunge.el.feature.CosineSimilarity;
import com.arunge.el.feature.EntityFeatureExtractor;
import com.arunge.el.feature.JaroWinkler;
import com.arunge.el.feature.LevenshteinEditDistance;
import com.arunge.el.feature.LongestCommonSubstringDistance;
import com.arunge.el.feature.StringMatchFeatureExtractor;
import com.arunge.el.feature.StringOverlapFeatureExtractor;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.processing.EntityPairInstanceConverter;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.ml.svm.SVMRank;
import com.mongodb.MongoClient;

public class TrainEntityLinker {

    private static Logger LOG = LoggerFactory.getLogger(TrainEntityLinker.class);
    
    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        EntityKBStore kbStore = new MongoEntityStore(client, "entity_store");
        EntityKBStore queryStore = new MongoEntityStore(client, "el_training_query_store");
        EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(kbStore);
        List<EntityFeatureExtractor> extractors = new ArrayList<>();
        extractors.add(new StringMatchFeatureExtractor(EntityAttribute.NAME));
        extractors.add(new StringMatchFeatureExtractor(EntityAttribute.CLEANSED_NAME));
        extractors.add(new StringOverlapFeatureExtractor(EntityAttribute.NAME));
        extractors.add(new StringOverlapFeatureExtractor(EntityAttribute.CLEANSED_NAME));
        extractors.add(new LevenshteinEditDistance(EntityAttribute.CLEANSED_NAME));
        extractors.add(new JaroWinkler(EntityAttribute.CLEANSED_NAME));
        extractors.add(new LongestCommonSubstringDistance(EntityAttribute.NAME));
        extractors.add(new LongestCommonSubstringDistance(EntityAttribute.CLEANSED_NAME));
        extractors.add(new CosineSimilarity(EntityAttribute.CONTEXT_VECTOR));
        EntityPairInstanceConverter instanceConverter = new EntityPairInstanceConverter(extractors);
        
        Path trainFile = Paths.get("output/test/train.dat");
        Path modelFile = Paths.get("output/test/model.model");
        Path outFile = Paths.get("output/test/train-error.eval");
        
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainFile.toFile()));
        
        CloseableIterator<KBEntity> trainingQueries = queryStore.allEntities();
        
        int numQueries = 1;
        int trainInstances = 0 ;
        while(trainingQueries.hasNext()) {
            int queryId = numQueries;
            KBEntity queryEntity = trainingQueries.next();
            System.out.println("Query Name: " + queryEntity.getName() + " " + numQueries);
            String goldId = queryEntity.getAttribute(EntityAttribute.GOLD_LABEL).getValueAsStr();
            if(goldId.equals("NIL")) {
                continue;
            }
            
            List<KBEntity> candidates = candidateRetrieval.retrieveCandidates(queryEntity).collect(Collectors.toList());
            LOG.info("Retrieved {} candidates", candidates.size());
            boolean goldIncluded = false;
            
            for(KBEntity e : candidates) {
                if(e.getId().equals(goldId)) {
                    goldIncluded = true;
                    break;
                }
            }
            if(!goldIncluded) { 
                Optional<KBEntity> gold = kbStore.fetchEntity(goldId);
                if(!gold.isPresent()) {
                    TextEntity missing = kbStore.fetchKBText(goldId).get();
                    LOG.info("Missing gold entity {} with id {}, infobox type is {}", missing.getName(), goldId, missing.getSingleMetadata(EntityMetadataKeys.INFOBOX_TYPE));
//                    throw new RuntimeException("Missing gold entity with id " + goldId + " in KB.");
                } else {
                    candidates.add(gold.get());
                }
            }
            
            List<String> instanceStrings = candidates.stream().map(candidate -> {
                Map<Integer, Double> instance = instanceConverter.convert(queryEntity, candidate);
                int rank = 1;
                if(candidate.getId().equals(goldId)) {
                    rank = 2;
                }
                String instanceStr = SVMRank.instanceToString(queryId, rank, instance);
                return instanceStr;
            }).collect(Collectors.toList());
            trainInstances += instanceStrings.size();
            Collections.sort(instanceStrings, Collections.reverseOrder());
            for(String inst : instanceStrings) {
                trainWriter.write(inst + "\n");
            }
            numQueries += 1;
        }
        LOG.info("Finished collecting training instances. Total training instances: {}", trainInstances);
        trainWriter.close();
        
        SVMRank rank = new SVMRank(Paths.get("J:\\Program Files\\SVMRank"));
        rank.train(trainFile, modelFile, "-c", "20");
        rank.eval(trainFile, modelFile, outFile);
    }
    
}
