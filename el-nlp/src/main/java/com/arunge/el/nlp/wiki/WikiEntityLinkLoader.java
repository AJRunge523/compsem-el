package com.arunge.el.nlp.wiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.collections.impl.factory.Sets;

import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.el.store.mongo.MongoNLPFields;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

public class WikiEntityLinkLoader {

    private static String linkPath = "J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\external_evals\\wiki_links";
    private static String backlinksPath = linkPath + "\\back_links_v2.txt";
    private static String outlinksPath = linkPath + "\\out_links.txt";
    
    
    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore kb = MongoEntityStore.kbStore(client);
        int maxIn = 0;
        int maxOut = 0;
        int maxOverlap = 0;
        double avgIn = 0;
        double avgOut = 0;
        double avgOverlap = 0;
        int total = 0;
        int numOver500 = 0;
        double inNorm = 126.0;
        double outNorm = 2589.0;
        Set<String> candIds = new HashSet<>(Files.readLines(new File("src/main/resources/candIds.txt"), Charsets.UTF_8));
        try(BufferedReader backLinkReader = new BufferedReader(new FileReader(new File(backlinksPath)));
                BufferedReader outLinkReader = new BufferedReader(new FileReader(new File(outlinksPath)))) {
            
            String backLinkLine = "";
            String outLinkLine = "";

            while((backLinkLine = backLinkReader.readLine()) != null && (outLinkLine = outLinkReader.readLine()) != null) {
                String[] backLinks = backLinkLine.split("\t");
                String[] outLinks = outLinkLine.split("\t");
                String backId = backLinks[0];
                String outId = outLinks[0];
                if(!backId.equals(outId)) {
                    System.out.println("Got offset: " + backId + ", " + outId);
                    if(backId.compareTo(outId) < 0) {
                        backLinkLine = backLinkReader.readLine();
                        backLinks = backLinkLine.split("\t");
                        backId = backLinks[0];
                        if(!backId.equals(outId)) {
                            throw new RuntimeException("AAAAH");
                        }
                    } else {
                        outLinkLine = outLinkReader.readLine();
                        outLinks = outLinkLine.split("\t");
                        outId = outLinks[0];
                        if(!backId.equals(outId)) {
                            throw new RuntimeException("AAAAH");
                        }
                    }
                }
                if(!candIds.contains(backId)) {
                    continue;
                }
                total += 1;
                Set<String> outSet = new HashSet<>();
                for(int i = 0; i < outLinks.length; i++) {
                    outSet.add(outLinks[i]);
                }
                Set<String> backSet = new HashSet<>();
                for(int i = 0; i < backLinks.length; i++) {
                    backSet.add(backLinks[i]);
                }
                if(backSet.size() > maxIn) {
                    maxIn = backSet.size();
                }
                if(outSet.size() > maxOut) {
                    maxOut = outSet.size();
                }
                if(outSet.size() > 2000 && candIds.contains(backId)) { 
                    numOver500 += 1;
                }
                Set<String> overlap = Sets.intersect(outSet, backSet);
                if(overlap.size() > maxOverlap) {
                    maxOverlap = overlap.size();
                }
                avgIn += backSet.size();
                avgOut += outSet.size();
                avgOverlap += overlap.size();
                
//                kb.updateNLPDocument(backId, MongoNLPFields.INLINKS, backSet.size() / inNorm);
//                kb.updateNLPDocument(backId, MongoNLPFields.OUTLINKS, outSet.size() / outNorm);
//                kb.updateNLPDocument(backId, MongoNLPFields.INREF_ENTITIES, backSet);
//                kb.updateNLPDocument(backId, MongoNLPFields.OUTREF_ENTITIES, outSet);
//                kb.updateNLPDocument(backId, MongoNLPFields.COREF_ENTITIES, overlap);
            }
        }
        avgIn = avgIn / total;
        avgOut = avgOut / total;
        avgOverlap = avgOverlap / total;
        System.out.println("Max in links: " + maxIn + ", Max out links: " + maxOut + ", Max Overlap: " + maxOverlap);
        System.out.println("Avg in links: " + avgIn + ", Avg out links: " + avgOut + ", Avg Overlap: " + avgOverlap);
        System.out.println("Total candidates found in link sets: " + total + " out of " + candIds.size());
//        System.out.println("Number of docs with more than 2000 outlinks: " + numOver500);
//        client.close();
    }
    
}
