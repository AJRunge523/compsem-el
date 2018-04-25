package com.arunge.el.processing.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class NEREvalMistakeChecker {

    public static void main(String[] args) throws Exception { 
        String outputFile = "output/runs/context-type-tests/no-context/el-eval-eval.txt";
        Set<String> nerMisses = new HashSet<>(Files.readLines(new File("output/ner-errors.txt"), Charsets.UTF_8));
        int total = 0;
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(outputFile)))) {
            String line = "";
            while((line = reader.readLine()) != null) {
                if(line.contains("*")) { 
                    String queryId = line.split("\t")[0];
                    if(nerMisses.contains(queryId)) {
                        total += 1;
                    }
                }
            }
        }
        System.out.println(nerMisses.size() + ", " + total);
    }
    
}
