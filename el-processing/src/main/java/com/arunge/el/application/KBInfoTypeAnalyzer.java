package com.arunge.el.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class KBInfoTypeAnalyzer {

    static Set<String> knownTypes = new HashSet<>();
    
    private static void initKnownTypes() {
        knownTypes.add("writer");
        knownTypes.add("cyclist");
        knownTypes.add("nba player");
        knownTypes.add("afl player");
        knownTypes.add("historic cricketer");
        knownTypes.add("_person");
        knownTypes.add("person");
        knownTypes.add("figure skater");
        knownTypes.add("actor");
        knownTypes.add("football biography");
        knownTypes.add("_president");
        knownTypes.add("gymnast");
        knownTypes.add("_indian_politician");
        knownTypes.add("playboy playmate");
        knownTypes.add("state representative");
        knownTypes.add("state_representative");
        knownTypes.add("_state_representative");
        knownTypes.add("prime minister");
        knownTypes.add("criminal");
        knownTypes.add("actress");
        knownTypes.add("ncaa athlete");
        knownTypes.add("_state_senator");
        knownTypes.add("_state senator");
        knownTypes.add("poker player");
        knownTypes.add("ambassador");
        knownTypes.add("mayor");
        knownTypes.add("chess player");
        knownTypes.add("nflactive");
        knownTypes.add("senator");
        knownTypes.add("character");
        knownTypes.add("_congressman");
        knownTypes.add("_actor");
        knownTypes.add("_vice president");
        knownTypes.add("radio presenter");
        knownTypes.add("chef");
        knownTypes.add("musical artist");
        
        knownTypes.add("ort in deutschland");
        knownTypes.add("settlement");
        knownTypes.add("swiss town");
        knownTypes.add("indian jurisdiction");
        knownTypes.add("u.s. state symbols");
        knownTypes.add("u.s. state");
        knownTypes.add("greek dimos");
        knownTypes.add("country or territory");
        knownTypes.add("province or territory of canada");
        knownTypes.add("german bundesland");
        knownTypes.add("country");
        knownTypes.add("prc province");
        knownTypes.add("palestinian authority muni");
        knownTypes.add("israel municipality");
        knownTypes.add("england county");
        knownTypes.add("autonomous region of china (prc)");
        knownTypes.add("uk place");
        knownTypes.add("city in afghanistan");
        
        knownTypes.add("_company");
        knownTypes.add("company");
        knownTypes.add("university");
        knownTypes.add("_university");
        knownTypes.add("football biography");
        knownTypes.add("_italian_political_party");
        knownTypes.add("american state political party");
        knownTypes.add("religious group");
        knownTypes.add("_american_political_party");
        knownTypes.add("american political party");
        knownTypes.add("newspaper");
        knownTypes.add("geopolitical organization");
        knownTypes.add("co-operative");
        knownTypes.add("organization");
        knownTypes.add("militant organization");
        knownTypes.add("law enforcement agency");
        knownTypes.add("american_political_party");
        knownTypes.add("_organization");
        knownTypes.add("football club");
        knownTypes.add("prc political parties");
        knownTypes.add("airport");
        knownTypes.add("exchange");
        knownTypes.add("firedepartment");
        knownTypes.add("_airline");
        knownTypes.add("un");
        knownTypes.add("radio station");
        knownTypes.add("central bank");
        knownTypes.add("bus transit");
        
        //Known unk types
        knownTypes.add("album");
        knownTypes.add("film");
        knownTypes.add("single");
        knownTypes.add("book");
        knownTypes.add("vg");
        knownTypes.add("television");
        knownTypes.add("_film");
        knownTypes.add("planet");
    }
    
    public static void main(String[] args) { 
        initKnownTypes();
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.kbStore(client);
        CloseableIterator<TextEntity> entities = store.allKBText();
        Map<String, Integer> unkInfoTypeCounts = new HashMap<>();
        int count = 0;
        while(entities.hasNext()) {
            count+=1;
            if(count % 10000 == 0) {
                System.out.println("Processed " + count + " entities");
            }
            TextEntity te = entities.next();
            if(te.getEntityType().equals("UKN")) {
                Optional<String> infoType = te.getSingleMetadata("info_type");
                if(!infoType.isPresent()) {
                    continue;
                }
                String infoTypeStr = infoType.get();
                if(knownTypes.contains(infoTypeStr)) {
                    continue;
                }
                if(!unkInfoTypeCounts.containsKey(infoTypeStr)) {
                    unkInfoTypeCounts.put(infoTypeStr, 1);
                } else {
                    unkInfoTypeCounts.put(infoTypeStr, unkInfoTypeCounts.get(infoTypeStr) + 1);
                }
            }
        }
        for(String key : unkInfoTypeCounts.keySet()) {
            System.out.println(key + "," + unkInfoTypeCounts.get(key));
        }
    }
    
}
