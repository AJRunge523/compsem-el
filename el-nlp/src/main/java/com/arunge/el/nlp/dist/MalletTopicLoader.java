package com.arunge.el.nlp.dist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.arunge.el.api.ContextType;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.mongodb.MongoClient;

public class MalletTopicLoader {

    public static void main(String[] args) throws IOException {
        File topicFile = new File("J:/Education/CMU/2018/Spring/Computational Semantics/Entity Linking/Topic Models/train_200.txt");
        int numTopics = 200;
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "el_training_query_store");
        BufferedReader reader = new BufferedReader(new FileReader(topicFile));
        String line = "";
        ContextType type = null;
        switch(numTopics) {
        case 50:
            type = ContextType.TOPIC_50;
            break;
        case 100:
            type = ContextType.TOPIC_100;
            break;
        case 200:
            type = ContextType.TOPIC_200;
            break;
        case 300:
            type = ContextType.TOPIC_300;
            break;
        case 400:
            type = ContextType.TOPIC_400;
            break;
        case 500:
            type = ContextType.TOPIC_500;
            break;
        default:
            throw new RuntimeException("Unexpected number of topics");
        }
        while ((line = reader.readLine()) != null) {
            if(line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split("\t");
            String file = parts[1];
            String fileparts[] = file.split("/");
            file = fileparts[fileparts.length - 1];
            file = file.replaceAll(".txt", "");
            System.out.println(file);
            Map<Integer, Double> sparseTopicVector = new HashMap<>();
            if(parts.length - 2 != numTopics) {
                throw new RuntimeException("Incorrect number of topics");
            }
            double sum = 0.0;
            for (int i = 2; i < parts.length; i++) {
                double topicWeight = Double.parseDouble(parts[i]);
                if(topicWeight > 0.01) {
                    sparseTopicVector.put(i-2, topicWeight);
                    sum += topicWeight;
                }
            }
            //Re-normalize
            for(Integer key : sparseTopicVector.keySet()) {
                sparseTopicVector.put(key, sparseTopicVector.get(key) / sum);
            }
            store.updateNLPDocument(file, type.name(), sparseTopicVector);
        }
        reader.close();
    }
}
