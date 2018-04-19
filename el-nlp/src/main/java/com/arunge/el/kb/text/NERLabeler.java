package com.arunge.el.kb.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.arunge.el.store.mongo.MongoEntityStore;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NERLabeler {

    private static StanfordCoreNLP pipeline;

    public static void main(String[] args) throws Exception { 
        initStanford();
        File queryTextDir = new File("J://entity-linking-data//eval-queries//");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output/train-ner.txt")));
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        for(File f : queryTextDir.listFiles()) {
            if(!f.getName().equals("EL003708.txt")) {
                continue;
            }
            String eName = store.fetchEntity(f.getName().replaceAll(".txt", "")).get().getName();
            writer.write(f.getName().replaceAll(".txt", "") + "\n");
            System.out.println(f.getName());
            List<String> lines = Files.readLines(f, Charsets.UTF_8);
            String content = lines.stream()/*.filter(line -> line.split("\\s+").length > 5).*/.reduce("", (a, b) -> a + "\n" + b);
            int index = content.replaceAll("\n", " ").indexOf(eName);
            if(index == -1) {
                System.out.println(eName);
                System.out.println(content);
                throw new RuntimeException("");
            }
            Annotation document = pipeline.process(content);
            Map<Integer, List<String>> mentionClusters = new HashMap<>();
            for(CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                for(CoreLabel label : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    if(eName.contains(label.originalText())) {
                        System.out.println(label + ", " + label.ner());
                    }
                }
            }
//            for(Integer key : mentionClusters.keySet()) {
//                for(String e : mentionClusters.get(key)) {
//                    writer.write(e);
//                }
//                writer.write("\n");
//            }
        }
        writer.close();
        client.close();
    }
    
    private static void initStanford() {
        String annotatorList = "tokenize,ssplit,pos,lemma,ner";
        Properties props = new Properties();
        props.setProperty("annotators",  annotatorList);
        pipeline = new StanfordCoreNLP(props);
    }
    
    
}
