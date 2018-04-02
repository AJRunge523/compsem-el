package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
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
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

/**
 * 
 *<p>Main access point for processing an Entity Linking dataset.<p>
 *
 * @author Andrew Runge
 *
 */
public class EntityLinkingEvaluation {

    private static Logger LOG = LoggerFactory.getLogger(EntityLinkingEvaluation.class);
    
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
        EntityPairInstanceConverter instanceConverter = new EntityPairInstanceConverter(extractors);
        
        SVMRank candidateRanker = new SVMRank(Paths.get("J:\\Program Files\\SVMRank"));
        
        Path goldFile = Paths.get("src/main/resources/train-gold.txt");
        
        Path modelFile = Paths.get("output/test/model.model");
        Path evalFile = Paths.get("output/test/query.eval");
        Path queryEvalFile = Paths.get("output/test/query-out.eval");
        Path outputFile = Paths.get("output/test/el-evaluation.txt");
        BufferedWriter evalWriter = new BufferedWriter(new FileWriter(outputFile.toFile()));
        CloseableIterator<KBEntity> trainingQueries = queryStore.allEntities();
        int numQueries = 0;
        while(trainingQueries.hasNext()) {
            KBEntity queryEntity = trainingQueries.next();
            numQueries += 1;
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(evalFile.toFile()));
            int queryId = numQueries;
            
            //Retrieve the candidates
            List<KBEntity> candidates = candidateRetrieval.retrieveCandidates(queryEntity).collect(Collectors.toList());
            
            //Write the candidate instances to a file for processing by the ranker
            List<String> instanceStrings = candidates.stream().map(candidate -> {
                Map<Integer, Double> instance = instanceConverter.convert(queryEntity, candidate);
                String instanceStr = SVMRank.instanceToString(queryId, 1, instance);
                return instanceStr;
            }).collect(Collectors.toList());
            for(String inst : instanceStrings) {
                trainWriter.write(inst + "\n");
            }
            trainWriter.close();
            
            //Rank the candidates
            candidateRanker.eval(evalFile, modelFile, queryEvalFile);
            
            //Identify the highest scoring candidate, or NIL if below a threshold.
            List<String> scores = Files.readLines(queryEvalFile.toFile(), Charsets.UTF_8);
            double maxScore = Double.NEGATIVE_INFINITY;
            KBEntity bestCandidate = null;
            for(int i = 0; i < scores.size(); i ++) {
                double score = Double.valueOf(scores.get(i));
                KBEntity candidate = candidates.get(i);
                if(score > maxScore) { 
                    maxScore = score;
                    bestCandidate = candidate;
                }
            }
            String bestId = "NIL";
            if(maxScore > 0) {
                bestId = bestCandidate.getId();
            }
            evalWriter.write(queryEntity.getId() + "\t" + bestId + "\n");
            if(numQueries % 100 == 0) {
                LOG.info("Evaluated {} queries.", numQueries);
            }
            
        }
        evalWriter.close();
        ELPerformanceEvaluator.evaluate(goldFile.toFile(), outputFile.toFile());
    }
    
}
