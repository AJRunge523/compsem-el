package com.arunge.el.application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ELPerformanceEvaluator {

    private static Logger LOG = LoggerFactory.getLogger(ELPerformanceEvaluator.class);
    
    public static void main(String[] args) throws IOException {
        File goldFile = new File("src/main/resources/dev-gold.txt");
        File testFile = new File("output/test/dev_eval.txt");
        ELPerformanceEvaluator.evaluate(goldFile, testFile);
    }
    
    public static void evaluate(File goldFile, File testFile) throws IOException {
        List<String> goldLines = Files.readLines(goldFile, Charsets.UTF_8);
        List<String> testLines = Files.readLines(testFile, Charsets.UTF_8);
        
        if(goldLines.size() != testLines.size()) {
            LOG.error("Unable to evaluate performance: Number of lines is different between gold and test file. Gold: {}, Test: {}", goldLines.size(), testLines.size());
            return;
        }
        //Used to evaluate average performance across all queries referring to same entity.
        Map<String, List<Integer>> entityMacroAccuracy = new HashMap<>();
        //Used to evaluate average performance across all queries with same entity type.
        Map<String, List<Integer>> entityTypeMacroAccuracy = new HashMap<>();
        //Used to evaluate NIL vs. non-NIL performance.
        Map<String, List<Integer>> elMicroAccuracy = new HashMap<>();
        for(int i = 0; i < goldLines.size(); i++) {
            String goldLine = goldLines.get(i);
            String[] goldParts = goldLine.split("\\s+");
            String goldQueryId = goldParts[0];
            String goldEntityId = goldParts[1];
            String goldEntityType = "";
            if(goldParts.length >= 3) {
                goldEntityType = goldParts[2];
            } 
            
            String testLine = testLines.get(i);
            String[] testParts = testLine.split("\\s+");
            String testQueryId = testParts[0];
            String testEntityId = testParts[1];
            if(!goldQueryId.equals(testQueryId)) {
                LOG.error("Unable to evaluate performance. Lines in test and gold files are not correctly aligned.");
                return;
            }
            boolean correct = false;
            if((goldEntityId.startsWith("NIL") && testEntityId.startsWith("NIL")) || goldEntityId.equals(testEntityId)) {
                correct = true;
            }
            if(correct) { 
                if(goldEntityId.startsWith("NIL")) { 
                    getOrAdd(elMicroAccuracy, "NIL", 1);
//                    getOrAdd(entityMacroAccuracy, "NIL", 1);
                } else {
                    getOrAdd(elMicroAccuracy, "KB", 1);
                    getOrAdd(entityMacroAccuracy, goldEntityId, 1);
                }
                getOrAdd(entityTypeMacroAccuracy, goldEntityType, 1);
            } else {
                if(goldEntityId.startsWith("NIL")) { 
                    getOrAdd(elMicroAccuracy, "NIL", 0);
//                    getOrAdd(entityMacroAccuracy, "NIL", 0);
                } else {
                    getOrAdd(elMicroAccuracy, "KB", 0);
                    getOrAdd(entityMacroAccuracy, goldEntityId, 0);
                }
                getOrAdd(entityTypeMacroAccuracy, goldEntityType, 0);
            }
        }
        int numKBCorrect = elMicroAccuracy.get("KB").stream().reduce(0, (a, b) -> a + b);
        int numKB = elMicroAccuracy.get("KB").size();
        double kbMicroAverage = ((double) numKBCorrect) / numKB;
        double numNILCorrect = 0;
        double nilMicroAverage = 0;
        int numNIL = 0;
        if(elMicroAccuracy.containsKey("NIL")) {
            numNIL = elMicroAccuracy.get("NIL").size();
            if(numNIL != 0) {
                numNILCorrect = elMicroAccuracy.get("NIL").stream().reduce(0, (a, b) -> a + b);
                nilMicroAverage = ((double) numNILCorrect) / numNIL;
            }
        }
        double totalAverage = (((double) numKBCorrect) + numNILCorrect) / (numKB + numNIL);
        int totalNumQueries = numKB + numNIL;
        LOG.info("Micro-Averages:");
        LOG.info("{} queries: {}", totalNumQueries, String.format("%.4f", totalAverage));
        LOG.info("{} KB: {}", numKB, String.format("%.4f", kbMicroAverage));
        LOG.info("{} NIL: {}", numNIL, String.format("%.4f", nilMicroAverage));
        
        int numPerCorrect = entityTypeMacroAccuracy.get("PER").stream().reduce(0,  (a, b) -> a + b);
        int numPer = entityTypeMacroAccuracy.get("PER").size();
        double perMicroAverage = ((double) numPerCorrect) / numPer;
        int numOrgCorrect = entityTypeMacroAccuracy.get("ORG").stream().reduce(0, (a, b) -> a + b);
        int numOrg = entityTypeMacroAccuracy.get("ORG").size();
        double orgMicroAverage = ((double) numOrgCorrect) / numOrg;
        int numGpeCorrect = entityTypeMacroAccuracy.get("GPE").stream().reduce(0, (a, b) -> a + b);
        int numGpe = entityTypeMacroAccuracy.get("GPE").size();
        double gpeMicroAverage = ((double) numGpeCorrect) / numGpe;
        
        LOG.info("Entity Type Macro Averages:");
        LOG.info("{} PER queries: {}", numPer, String.format("%.4f", perMicroAverage));
        LOG.info("{} GPE queries: {}", numGpe, String.format("%.4f", gpeMicroAverage));
        LOG.info("{} ORG queries: {}", numOrg, String.format("%.4f", orgMicroAverage));
        
        int numEntities = entityMacroAccuracy.size();
        double macrosum = 0.0;
        for(String eId : entityMacroAccuracy.keySet()) {
            List<Integer> ans = entityMacroAccuracy.get(eId);
            double numCorrect = ans.stream().reduce(0, (a, b) -> a + b);
            numCorrect /= ans.size();
            macrosum += numCorrect;
        }
        macrosum /= numEntities;
        LOG.info("Macro-Averages:");
        LOG.info("{} gold entities: {}", numEntities, String.format("%.4f", macrosum));
    }
    
    private static void getOrAdd(Map<String, List<Integer>> map, String key, int value) {
        if(!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        } 
        map.get(key).add(value);
    }
    
}
