package com.arunge.el.nlp.entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.arunge.el.api.KBEntity;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.api.StringIndexer;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.FilteredTokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.tokenization.TokenFilters;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;
import com.arunge.unmei.iterators.CloseableIterator;
import com.arunge.unmei.iterators.Iterators;
import com.mongodb.MongoClient;

public class CorefEntitiesInfoboxDistComputer {

    private static Tokenizer tokenizer;
    
    public static void main(String[] args) throws IOException { 
        
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore kb = MongoEntityStore.kbStore(client);
        MongoEntityStore queries = MongoEntityStore.trainStore(client);
        List<TokenFilter> filters = new ArrayList<>();
        filters.add(TokenFilters.stopwords());
        filters.add(TokenFilters.punctuation());
        filters.add(TokenFilters.maxLength(50));
        tokenizer = new FilteredTokenizer(Tokenizers.getDefault(), filters);
        CloseableIterator<NLPDocument> queryNLP = queries.allNLPDocuments();
        StringIndexer infoboxIndexer = loadInfoboxIndexer(new File("output/entity-vectors/v2/infobox-cn-indexes.txt"));
        try(BufferedWriter ibVectorWriter = new BufferedWriter(new FileWriter(new File("output/entity-vectors/v2/infobox-train-cn-vectors.txt")))) {
            while(queryNLP.hasNext()) {
                NLPDocument doc = queryNLP.next();
                Map<Integer, Double> infoboxVector = new HashMap<>();
                Set<String> corefEntities = doc.getCorefEntities();
                int totalEnts = 0;
                for(String c : corefEntities) {
                    String cleansed = getCanonicalName(c);
                    List<KBEntity> entities = Iterators.toStream(kb.queryByName(cleansed)).collect(Collectors.toList());
//                    if(entities.size() > 1) {
//                        System.out.println("Multiple entities for query " + c + ": " + entities.size());
//                        continue;
//                    } 
                    for(KBEntity kbEnt : entities) {
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
                ibVectorWriter.write(doc.getId() + "\t");
                for(int i : infoboxVector.keySet()) {
                    ibVectorWriter.write(i + ":" + (infoboxVector.get(i) / totalEnts) + "\t");
                }
                ibVectorWriter.write("\n");
            }
        }
    }
    
    private static String getCanonicalName(String name) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("-", " ");
        clean = clean.replaceAll("\\p{Punct}", "");
        clean = clean.toLowerCase();
        clean = tokenizer.tokenize(clean).map(t -> t.text()).reduce("", (a, b) -> a + " " + b);
        return clean.trim();
    }
    
    private static StringIndexer loadInfoboxIndexer(File file) throws IOException {
        StringIndexer idx = new StringIndexer();
        Map<Integer, String> reverseIndex = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) { 
            String line = "";
            while((line = reader.readLine()) != null) {
                String[] split = line.split("\t");
                reverseIndex.put(Integer.parseInt(split[1]), split[0]);
            }
        }
        for(int i = 0; i < reverseIndex.size(); i++) {
            idx.getOrAdd(reverseIndex.get(i));
        }
        return idx;
    }
    
}
