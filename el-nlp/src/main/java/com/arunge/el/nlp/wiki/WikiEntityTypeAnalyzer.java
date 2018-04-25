package com.arunge.el.nlp.wiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.Sets;

import com.arunge.el.api.KBEntity;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.api.StringIndexer;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.FilteredTokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.tokenization.TokenFilters;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;
import com.arunge.unmei.iterators.Iterators;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

public class WikiEntityTypeAnalyzer {

    private static String linkPath = "J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\external_evals\\wiki_links";
    private static String backlinksPath = linkPath + "\\back_links_v2.txt";
    private static String outlinksPath = linkPath + "\\out_links.txt";
    
    private static Tokenizer tokenizer;
    
    public static void main(String[] args) throws IOException {
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore kb = MongoEntityStore.kbStore(client);
        int total = 0;
        List<TokenFilter> filters = new ArrayList<>();
        filters.add(TokenFilters.stopwords());
        filters.add(TokenFilters.punctuation());
        filters.add(TokenFilters.maxLength(50));
        tokenizer = new FilteredTokenizer(Tokenizers.getDefault(), filters);
        Set<String> candIds = new HashSet<>(Files.readLines(new File("src/main/resources/candIds.txt"), Charsets.UTF_8));
        StringIndexer infoboxIndexer = new StringIndexer();
        try(BufferedReader backLinkReader = new BufferedReader(new FileReader(new File(backlinksPath)));
                BufferedReader outLinkReader = new BufferedReader(new FileReader(new File(outlinksPath)));
                BufferedWriter ibVectorWriter = new BufferedWriter(new FileWriter(new File("output/infobox-cn-vectors.txt")))) {
            
            String backLinkLine = "";
            String outLinkLine = "";

            while((backLinkLine = backLinkReader.readLine()) != null && (outLinkLine = outLinkReader.readLine()) != null) {
                Map<Integer, Double> infoboxVector = new HashMap<>();
                String[] backLinks = backLinkLine.split("\t");
                String[] outLinks = outLinkLine.split("\t");
                String backId = backLinks[0];
                String outId = outLinks[0];
                if(!backId.equals(outId)) {
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
                if(total % 500 == 0) { 
                    System.out.println("Processed " + total);
                }
                Set<String> outSet = new HashSet<>();
                for(int i = 0; i < outLinks.length; i++) {
                    outSet.add(outLinks[i]);
                }
                Set<String> backSet = new HashSet<>();
                for(int i = 0; i < backLinks.length; i++) {
                    backSet.add(backLinks[i]);
                }
                Set<String> overlap = Sets.intersect(outSet, backSet);
                int totalEnts = 0;
                System.out.println(backId);
                for(String e : overlap) {
                    String cleansed = getCanonicalName(e);
                    List<KBEntity> ents = Iterators.toStream(kb.queryByName(cleansed)).collect(Collectors.toList());
//                    if(ents.size() > 1) {
//                        System.out.println("Multiple entities for query " + e + ": " + ents.size());
//                    } 
                    for(KBEntity kbEnt : ents) {
                        TextEntity text = kb.fetchKBText(kbEnt.getId()).get();
                        Optional<String> infoType = text.getSingleMetadata("info_type");
                        if(infoType.isPresent()) {
                            String type = infoType.get().toLowerCase().replaceAll("_", " ").trim().replaceAll("\\p{Punct}+", "");
                            if(type.isEmpty()) {
                                continue;
                            }
                            totalEnts += 1;
                            int index = infoboxIndexer.getOrAdd(type);
                            if(!infoboxVector.containsKey(index)) { 
                                infoboxVector.put(index, 1.0);
                            } else {
                                infoboxVector.put(index, infoboxVector.get(index) + 1);
                            }
                        }
                    }
                }
                ibVectorWriter.write(backId + "\t");
                for(int i : infoboxVector.keySet()) {
                    ibVectorWriter.write(i + ":" + (infoboxVector.get(i) / totalEnts) + "\t");
                }
                ibVectorWriter.write("\n");
            }
            ibVectorWriter.write("====================================\n");
            for(Map.Entry<String, Integer> entry : infoboxIndexer) {
                ibVectorWriter.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        }
        client.close();
    }
    
    private static String getCanonicalName(String name) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("-", " ");
        clean = clean.replaceAll("\\p{Punct}", "");
        clean = clean.toLowerCase();
        clean = tokenizer.tokenize(clean).map(t -> t.text()).reduce("", (a, b) -> a + " " + b);
        return clean.trim();
    }
}
