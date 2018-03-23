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

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.feature.EntityFeatureExtractor;
import com.arunge.el.feature.StringMatchFeatureExtractor;
import com.arunge.el.feature.StringOverlapFeatureExtractor;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.processing.EntityInstanceConverter;
import com.arunge.el.processing.KBDocumentTextProcessor;
import com.arunge.el.processing.KBEntityConverter;
import com.arunge.el.query.QuerySetLoader;
import com.arunge.el.store.mongo.MongoEntityStore;
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

    public static void main(String[] args) throws IOException {
        EntityKBStore entityStore = new MongoEntityStore(new MongoClient("localhost", 27017), "entity_store");
        KBDocumentTextProcessor textProcessor = new KBDocumentTextProcessor();
        KBEntityConverter entityConverter = new KBEntityConverter();
        EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(entityStore);
        
        List<EntityFeatureExtractor> extractors = new ArrayList<>();
        extractors.add(new StringMatchFeatureExtractor(EntityAttribute.NAME));
        extractors.add(new StringMatchFeatureExtractor(EntityAttribute.CLEANSED_NAME));
        extractors.add(new StringOverlapFeatureExtractor(EntityAttribute.NAME));
        extractors.add(new StringOverlapFeatureExtractor(EntityAttribute.CLEANSED_NAME));
        EntityInstanceConverter instanceConverter = new EntityInstanceConverter(extractors);
        
        SVMRank candidateRanker = new SVMRank(Paths.get("J:\\Program Files\\SVMRank"));
        
        Path goldFile = Paths.get("src/test/resources/train-gold.txt");
        
        Path modelFile = Paths.get("output/test/model.model");
        Path evalFile = Paths.get("output/test/query.eval");
        Path queryEvalFile = Paths.get("output/test/query-out.eval");
        Path outputFile = Paths.get("output/test/el-evaluation.txt");
        BufferedWriter evalWriter = new BufferedWriter(new FileWriter(outputFile.toFile()));
        Iterable<ELQuery> queries = QuerySetLoader.loadTAC2010Train();
        int numQueries = 0;
        for(ELQuery query : queries) {
            numQueries += 1;
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(evalFile.toFile()));
            int queryId = numQueries;
            
            //Process the query through the NLP and information extraction pipelines.
            TextEntity textEntity = query.convertToEntity();
            NLPDocument nlp = textProcessor.process(textEntity);
            KBEntity queryEntity = entityConverter.convert(textEntity, nlp);
            
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
            evalWriter.write(query.getQueryId() + "\t" + bestId + "\n");
            
        }
        evalWriter.close();
        ELPerformanceEvaluator.evaluate(goldFile.toFile(), outputFile.toFile());
    }
    
}
