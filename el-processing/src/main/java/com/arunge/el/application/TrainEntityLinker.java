package com.arunge.el.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.EntityMetadataKeys;
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
import com.mongodb.MongoClient;

public class TrainEntityLinker {

    private static Logger LOG = LoggerFactory.getLogger(TrainEntityLinker.class);
    
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
        
        Path trainFile = Paths.get("output/test/train.dat");
        Path modelFile = Paths.get("output/test/model.model");
        
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter(trainFile.toFile()));
        
        Iterable<ELQuery> queries = QuerySetLoader.loadTAC2010Train();
        int numQueries = 1;
        for(ELQuery query : queries) {
            int queryId = numQueries;
            if(numQueries % 100 == 0) {
                System.out.println("Loaded " + numQueries + " queries.");
            }
            TextEntity textEntity = query.convertToEntity();
            String goldId = textEntity.getSingleMetadata("gold").get();
            if(textEntity.getSingleMetadata("gold").get().equals("NIL")) {
                continue;
            }
            NLPDocument nlp = textProcessor.process(textEntity);
            KBEntity queryEntity = entityConverter.convert(textEntity, nlp);
            
            List<KBEntity> entities = candidateRetrieval.retrieveCandidates(queryEntity).collect(Collectors.toList());
            boolean goldIncluded = false;
            
            for(KBEntity e : entities) {
                if(e.getId().equals(goldId)) {
                    goldIncluded = true;
                    break;
                }
            }
            if(!goldIncluded) { 
                Optional<KBEntity> gold = entityStore.fetchEntity(goldId);
                if(!gold.isPresent()) {
                    TextEntity missing = entityStore.fetchKBText(goldId).get();
                    LOG.info("Missing gold entity {} with id {}, infobox type is {}", missing.getName(), goldId, missing.getSingleMetadata(EntityMetadataKeys.INFOBOX_TYPE));
//                    throw new RuntimeException("Missing gold entity with id " + goldId + " in KB.");
                } else {
                    entities.add(gold.get());
                }
            }
            
            List<String> instanceStrings = entities.stream().map(candidate -> {
                Map<Integer, Double> instance = instanceConverter.convert(queryEntity, candidate);
                int rank = 1;
                if(candidate.getId().equals(goldId)) {
                    rank = 2;
                }
                String instanceStr = SVMRank.instanceToString(queryId, rank, instance);
                return instanceStr;
            }).collect(Collectors.toList());
            
            for(String inst : instanceStrings) {
                trainWriter.write(inst + "\n");
            }
            numQueries += 1;
        }
        
        trainWriter.close();
    }
    
}
