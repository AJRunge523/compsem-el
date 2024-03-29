package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.DenseVectorAttribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.el.nlp.dist.ContextStore;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.processing.EntityPairInstanceConverter;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.ml.svm.SVMRank;
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
    
    private MongoClient client;
    private ContextStore contexts;
    private EntityCandidateRetrievalEngine candidateRetrieval;
    private ContextType[] contextTypes;
    
    public EntityLinkingEvaluation(MongoClient client, ContextStore contexts, 
            EntityCandidateRetrievalEngine candidateRetrieval, ContextType... contextTypes) { 
        this.client = client;
        this.candidateRetrieval = candidateRetrieval;
        this.contextTypes = contextTypes;
        this.contexts = contexts;
    }
    
    public void evalEval(String evalFileName, boolean evalNil) throws IOException {
        EntityKBStore queryStore = MongoEntityStore.evalStore(client); 
        
        EntityPairInstanceConverter instanceConverter = EntityPairInstanceConverter.currentSet();
        
        Path goldFile = Paths.get("src/main/resources/eval-gold.txt");
        if(!evalNil) { 
            goldFile = Paths.get("src/main/resources/eval-nonil-gold.txt");
        }
        
        Path modelFile = Paths.get("output/test/model.model");
        
        Path outputFile = Paths.get("output/test/", evalFileName);
        evaluate(queryStore.allEntities(), instanceConverter, evalNil, modelFile, outputFile, goldFile);
    }
    
    public void evalDev(String evalFileName, Set<String> devIds, boolean evalNil) throws IOException {
        EntityKBStore queryStore = MongoEntityStore.trainStore(client); 
        EntityPairInstanceConverter instanceConverter = EntityPairInstanceConverter.currentSet();
        
        Path goldFile = Paths.get("src/main/resources/dev-gold.txt");
        if(!evalNil) { 
            goldFile = Paths.get("src/main/resources/dev-nonil-gold.txt");
        }
        
        Path modelFile = Paths.get("output/test/model.model");
        Path outputFile = Paths.get("output/test/", evalFileName);
        evaluate(queryStore.allEntities(), devIds, new HashSet<>(), instanceConverter, evalNil, modelFile, outputFile, goldFile);
    }
    
    public void evalTrain(String evalFileName, boolean evalNil) throws IOException {
        EntityKBStore queryStore = MongoEntityStore.trainStore(client); 
        
        EntityPairInstanceConverter instanceConverter = EntityPairInstanceConverter.currentSet();
        
        Path goldFile = Paths.get("src/main/resources/train-gold-full.txt");
        if(!evalNil) { 
            goldFile = Paths.get("src/main/resources/train-nonil-gold-full.txt");
        }
        
        Path modelFile = Paths.get("output/test/model.model");
        Path outputFile = Paths.get("output/test/", evalFileName);
        evaluate(queryStore.allEntities(), instanceConverter, evalNil, modelFile, outputFile, goldFile);
    }
    
    public void evalTrain(String evalFileName, Set<String> devIds, boolean evalNil) throws IOException {
        EntityKBStore queryStore = MongoEntityStore.trainStore(client); 
        
        EntityPairInstanceConverter instanceConverter = EntityPairInstanceConverter.currentSet();
        
        Path goldFile = Paths.get("src/main/resources/train-gold.txt");
        if(!evalNil) { 
            goldFile = Paths.get("src/main/resources/train-nonil-gold.txt");
        }
        
        Path modelFile = Paths.get("output/test/model.model");
        Path outputFile = Paths.get("output/test/", evalFileName);
        evaluate(queryStore.allEntities(), new HashSet<>(), devIds, instanceConverter, evalNil, modelFile, outputFile, goldFile);
    }
    
    private void evaluate(CloseableIterator<KBEntity> queryIter, EntityPairInstanceConverter instanceConverter, boolean evalNil,  
            Path modelFile, Path outputFile, Path goldFile) throws IOException {
        evaluate(queryIter, new HashSet<>(), new HashSet<>(), instanceConverter, evalNil, modelFile, outputFile, goldFile);
    }
    private void evaluate(CloseableIterator<KBEntity> queryIter, Set<String> idsToEval, Set<String> idsToSkip, 
            EntityPairInstanceConverter instanceConverter, boolean evalNil, Path modelFile, Path outputFile, Path goldFile) throws IOException {
        Path evalFile = Paths.get("output/test/query.eval");
        Path queryEvalFile = Paths.get("output/test/query-out.eval");
        SVMRank candidateRanker = new SVMRank(Paths.get("J:\\Program Files\\SVMRank"));

        BufferedWriter evalWriter = new BufferedWriter(new FileWriter(outputFile.toFile()));
        int numQueries = 0;
        while(queryIter.hasNext()) {
            KBEntity queryEntity = queryIter.next();
            if(!idsToEval.isEmpty() && !idsToEval.contains(queryEntity.getId())) {
                continue;
            } else if(idsToSkip.contains(queryEntity.getId())) {
                continue;
            }
            for(ContextType contextType : contextTypes) {
                if(contextType != ContextType.NORM_TFIDF) {
                    double[] context = contexts.getContext(contextType, queryEntity.getId());
                    queryEntity.setAttribute(EntityAttribute.valueOf(contextType.name()), DenseVectorAttribute.valueOf(context));
                }
            }
            numQueries += 1;
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(evalFile.toFile()));
            int queryId = numQueries;
            
            String goldId = null;
            Attribute goldAttr = queryEntity.getAttribute(EntityAttribute.GOLD_LABEL);
            if(goldAttr != null) {
                goldId = goldAttr.getValueAsStr();
            }
            if(goldId.startsWith("NIL") && !evalNil) {
                continue;
            }
            
            //Retrieve the candidates
            List<KBEntity> candidates = candidateRetrieval.retrieveCandidates(queryEntity, contextTypes).collect(Collectors.toList());
            candidates.add(new KBEntity("NIL"));
            //Write the candidate instances to a file for processing by the ranker
            List<String> instanceStrings = candidates.stream().map(candidate -> {
                String info = candidate.getId();
                Map<Integer, Double> instance = null;
                if(candidate.getId().equals("NIL")) {
                    instance = instanceConverter.createNil();
                } else {
                    instance = instanceConverter.convert(queryEntity, candidate);
                }
                String instanceStr = SVMRank.instanceToString(queryId, 1, instance, info);
                return instanceStr;
            }).collect(Collectors.toList());
            for(String inst : instanceStrings) {
                trainWriter.write(inst + "\n");
            }
            trainWriter.close();
            
            //Rank the candidates
            List<Double> scores = candidateRanker.eval(evalFile, modelFile, queryEvalFile);
            if(scores.size() == 0) { 
                throw new RuntimeException("Error while scoring results for query " + queryEntity.getId());
            }
            //Identify the highest scoring candidate, or NIL if below a threshold.
            double maxScore = Double.NEGATIVE_INFINITY;
            KBEntity bestCandidate = null;
            for(int i = 0; i < scores.size(); i ++) {
                double score = scores.get(i);
                KBEntity candidate = candidates.get(i);
                if(score > maxScore) { 
                    maxScore = score;
                    bestCandidate = candidate;
                }
            }
            String bestId = bestCandidate.getId();
//            if(maxScore > 0) {
//                bestId = bestCandidate.getId();
//            }
            Attribute candName = bestCandidate.getAttribute(EntityAttribute.NAME);
            if(candName == null) {
                candName = StringAttribute.valueOf("NIL");
            }
            evalWriter.write(queryEntity.getId() + "\t" + bestId + (goldId != null? "\t" + goldId : "") + 
                    (bestId.equals(goldId) ? "" : "\t*\t" + queryEntity.getAttribute(EntityAttribute.GOLD_NER) + "\t" + queryEntity.getType() + "\t" + candName + "\t" + queryEntity.getName()) + "\n");
            if(numQueries % 100 == 0) {
                LOG.info("Evaluated {} queries.", numQueries);
            }
            
        }
        evalWriter.close();
        ELPerformanceEvaluator.evaluate(goldFile.toFile(), outputFile.toFile());
    }
    
}
