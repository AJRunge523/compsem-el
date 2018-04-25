package com.arunge.el.nlp.entities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.coref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class ExtractEntities {

    private static StanfordCoreNLP pipeline;

    public static void main(String[] args) throws Exception { 
        initStanford();
        File queryTextDir = new File("J://entity-linking-data//train-queries//");
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("output/train-entities-v2.txt")));
        for(File f : queryTextDir.listFiles()) {
            writer.write(f.getName().replaceAll(".txt", "") + "\n");
            System.out.println(f.getName());
            List<String> lines = Files.readLines(f, Charsets.UTF_8);
            String content = lines.stream().reduce("", (a, b) -> a + "\n" + b);
            Annotation document = pipeline.process(content);
            Set<Integer> corefIds = new HashSet<>();
            Map<Integer, String> repMentions = new HashMap<>();
            for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
                CorefMention cm = cc.getRepresentativeMention();
                corefIds.add(cc.getChainID());
                repMentions.put(cc.getChainID(), cm.toString());
            }
            Map<Integer, List<String>> mentionClusters = new HashMap<>();
            for(CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                List<Mention> mentions = sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class);
                for(Mention m : mentions) {
                    if(m.nerString.equals("LOCATION") || m.nerString.equals("PERSON") || m.nerString.equals("ORGANIZATION")) {
                        if(!mentionClusters.containsKey(m.corefClusterID)) {
                            mentionClusters.put(m.corefClusterID, new ArrayList<>());
                        }
                        String text = m.nerName();
                        String ner = m.nerString;
                        String entry = "\t" + text + "\t" + ner + "\n";
                        if(repMentions.containsKey(m.corefClusterID)) {
                            if(text.equals(repMentions.get(m.corefClusterID))) { 
                                mentionClusters.get(m.corefClusterID).add(0, entry);
                                continue;
                            }
                        }
                        mentionClusters.get(m.corefClusterID).add("\t" + text + "\t" + ner);
                    }
                }
            }
            for(Integer key : mentionClusters.keySet()) {
                for(String e : mentionClusters.get(key)) {
                    writer.write(e);
                }
                writer.write("\n");
            }
        }
        writer.close();
        
    }
    
    private static void initStanford() {
        String annotatorList = "tokenize,ssplit,pos,lemma,ner,parse,mention,coref";
        Properties props = new Properties();
        props.setProperty("annotators",  annotatorList);
        props.setProperty("coref.algorithm", "neural");
        pipeline = new StanfordCoreNLP(props);
    }
    
}
