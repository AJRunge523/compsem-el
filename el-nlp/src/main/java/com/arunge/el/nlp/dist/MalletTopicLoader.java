package com.arunge.el.nlp.dist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MalletTopicLoader {

    public static void main(String[] args) throws IOException {
        File topicFile = new File("src/main/resources/eval_topics.txt");
        BufferedReader reader = new BufferedReader(new FileReader(topicFile));
        String line = "";
        reader.readLine();
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\t");
            String id = parts[0];
            String file = parts[1];
            String fileparts[] = file.split("/");
            file = fileparts[fileparts.length - 1];
            System.out.print(id + ": " + file + " --> ");
            for (int i = 2; i < parts.length; i++) {
                System.out.print(Double.parseDouble(parts[i]) + ", ");
            }
            System.out.print("\n");
        }
    }
}
