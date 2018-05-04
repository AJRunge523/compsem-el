package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.DenseVectorAttribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.nlp.dist.ContextStore;
import com.arunge.el.processing.EntityCandidateRetrievalEngine;
import com.arunge.el.processing.EntityPairInstanceConverter;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.ml.svm.SVMRank;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

public class TrainEntityLinker {

    private static Logger LOG = LoggerFactory.getLogger(TrainEntityLinker.class);
    
    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        EntityKBStore kbStore = MongoEntityStore.kbStore(client);
        EntityKBStore queryStore = MongoEntityStore.trainStore(client);
        ContextStore contexts = ContextStore.getDefault();
        EntityCandidateRetrievalEngine candidateRetrieval = new EntityCandidateRetrievalEngine(kbStore, contexts);
        
        ContextType[] contextTypes = { ContextType.FULL_EMB, ContextType.FULL_EMB_TFIDF, ContextType.WINDOW_EMB, ContextType.WINDOW_EMB_TFIDF };
        
        EntityPairInstanceConverter instanceConverter = EntityPairInstanceConverter.currentSet();
        
        Path trainFile = Paths.get("output/test/eval.dat");
        Path modelFile = Paths.get("output/test/model.model");
        
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainFile.toFile()));
        Set<String> devIds = Sets.newHashSet(Files.readLines(new File("src/main/resources/dev-ids.txt"), Charsets.UTF_8));
        
        CloseableIterator<KBEntity> trainingQueries = queryStore.allEntities();
        
        boolean evalNil = true;
        
        boolean trainWithDev = false;
        
        int numQueries = 1;
        int trainInstances = 0 ;
        while(trainingQueries.hasNext()) {
            int queryId = numQueries;
            KBEntity queryEntity = trainingQueries.next();
            if(trainWithDev && !devIds.contains(queryEntity.getId())) { 
                continue;
            } else if(!trainWithDev && devIds.contains(queryEntity.getId())) {
                continue;
            }
            for(ContextType contextType : contextTypes) {
                if(contextType != ContextType.NORM_TFIDF) {
                    double[] context = contexts.getContext(contextType, queryEntity.getId());
                    queryEntity.setAttribute(EntityAttribute.valueOf(contextType.name()), DenseVectorAttribute.valueOf(context));
                }
            }
            

            System.out.println("Query Name: " + queryEntity.getName() + " " + numQueries);
            String goldId = queryEntity.getAttribute(EntityAttribute.GOLD_LABEL).getValueAsStr();
            String goldNER = queryEntity.getAttribute(EntityAttribute.GOLD_NER).getValueAsStr();
            
            List<KBEntity> candidates = candidateRetrieval.retrieveCandidates(queryEntity, contextTypes).collect(Collectors.toList());
            if(evalNil) { 
                KBEntity nil = new KBEntity("NIL");
                nil.setName("NIL");
                candidates.add(nil);
            }
            LOG.info("Retrieved {} candidates", candidates.size());
            boolean goldIncluded = false;
            String goldName = "";
            
            for(KBEntity e : candidates) {
                if(e.getId().equals(goldId) ) {
                    goldName = e.getName();
                    goldIncluded = true;
                    break;
                }
            }
            String finalGoldName = goldName;
            //If we're not going to find the gold entity correctly, then we'll skip training on this instance altogether to avoid pathological training points
//            if(!goldIncluded) {
//                continue;
//                Optional<KBEntity> gold = kbStore.fetchEntity(goldId);
//                if(!gold.isPresent()) {
//                    TextEntity missing = kbStore.fetchKBText(goldId).get();
//                    LOG.info("Missing gold entity {} with id {}, infobox type is {}", missing.getName(), goldId, missing.getSingleMetadata(EntityMetadataKeys.INFOBOX_TYPE));
////                    throw new RuntimeException("Missing gold entity with id " + goldId + " in KB.");
//                } else {
//                    candidates.add(gold.get());
//                }
//            }
            
            List<String> instanceStrings = candidates.stream().map(candidate -> {
                
                Map<Integer, Double> instance = null;
                String info = candidate.getId() + "_" + goldId + "_" + goldNER + "_" + candidate.getName() + "_" + finalGoldName;
                if(candidate.getId().equals("NIL")) {
                    instance = instanceConverter.createNil();
                } else {
                    instance = instanceConverter.convert(queryEntity, candidate);
                }
                int rank = 1;
                if(candidate.getId().equals(goldId)) {
                    rank = 2;
                }
                String instanceStr = SVMRank.instanceToString(queryId, rank, instance, info);
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
        instanceConverter.writeFeatures("output/test/model.features");
        trainWriter.close();
        
//        SVMRank rank = new SVMRank(Paths.get("J:\\Program Files\\SVMRank"));
//        rank.train(trainFile, modelFile, "-c", "20");
//        EntityLinkingEvaluation eval = new EntityLinkingEvaluation(client, contexts, candidateRetrieval, contextTypes);
//        if(!trainWithDev) { 
//            eval.evalTrain("el-eval-train.txt", devIds, evalNil);
//            eval.evalDev("el-eval-dev.txt", devIds, evalNil);
//        } else {
//            eval.evalTrain("el-eval-train.txt", evalNil);
//        }
//        eval.evalEval("el-eval-eval.txt", evalNil);
        client.close();
    }
}
