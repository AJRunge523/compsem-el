package com.arunge.el.processing;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.arunge.el.api.EntityMetadataKeys;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.TextEntity;

public class EntityTypeConverter {

    static Set<String> personTypes;
    static Set<String> gpeTypes;
    static Set<String> orgTypes;
    static Set<String> personArtists;
    static Set<String> orgArtists;
    
    
    static {
        personTypes = new HashSet<>();
        personTypes.add("writer");
//        personTypes.add("musical artist");
        personTypes.add("cyclist");
        personTypes.add("nba player");
        personTypes.add("afl player");
        personTypes.add("historic cricketer");
        personTypes.add("_person");
        personTypes.add("person");
        personTypes.add("figure skater");
        personTypes.add("actor");
        personTypes.add("football biography");
        personTypes.add("_president");
        personTypes.add("gymnast");
        personTypes.add("_indian_politician");
        personTypes.add("playboy playmate");
        personTypes.add("state representative");
        personTypes.add("state_representative");
        personTypes.add("_state_representative");
        personTypes.add("prime minister");
        personTypes.add("criminal");
        personTypes.add("actress");
        personTypes.add("ncaa athlete");
        personTypes.add("_state_senator");
        personTypes.add("_state senator");
        personTypes.add("poker player");
        personTypes.add("ambassador");
        personTypes.add("mayor");
        
        gpeTypes = new HashSet<>();
        gpeTypes.add("ort in deutschland");
        gpeTypes.add("settlement");
        gpeTypes.add("swiss town");
        gpeTypes.add("indian jurisdiction");
        gpeTypes.add("u.s. state symbols");
        gpeTypes.add("u.s. state");
        gpeTypes.add("greek dimos");
        gpeTypes.add("country or territory");
        gpeTypes.add("province or territory of canada");
        gpeTypes.add("german bundesland");
        gpeTypes.add("country");
        gpeTypes.add("prc province");
        gpeTypes.add("palestinian authority muni");
        gpeTypes.add("israel municipality");
        orgTypes = new HashSet<>();
        orgTypes.add("_company");
        orgTypes.add("company");
        orgTypes.add("university");
        orgTypes.add("_university");
        orgTypes.add("football biography");
        orgTypes.add("_italian_political_party");
        orgTypes.add("american state political party");
        orgTypes.add("religious group");
        orgTypes.add("_american_political_party");
        orgTypes.add("american political party");
        orgTypes.add("newspaper");
        orgTypes.add("geopolitical organization");
        
        personArtists = new HashSet<>();
        personArtists.add("solo_singer");
        personArtists.add("non_vocal_instrumentalist");
        personArtists.add("Malaysian_actres");
        orgArtists = new HashSet<>();
        orgArtists.add("group_or_band");
    }
    
    
    public static EntityType convert(TextEntity text) {
        String entityType = text.getEntityType();
        if(entityType.toLowerCase().equals("per")) {
            return EntityType.PERSON;
        } else if(entityType.toLowerCase().equals("gpe")) {
            return EntityType.GPE;
        } else if(entityType.toLowerCase().equals("org")) {
            return EntityType.ORG;
        } else {
            Optional<String> infoboxTypeOpt = text.getSingleMetadata(EntityMetadataKeys.INFOBOX_TYPE);
            if(!infoboxTypeOpt.isPresent()) {
                return EntityType.UNK;
            }
            String infoboxType = infoboxTypeOpt.get();
            if(personTypes.contains(infoboxType)) {
                return EntityType.PERSON;
            } else if(gpeTypes.contains(infoboxType)) {
                return EntityType.GPE;
            } else if(orgTypes.contains(infoboxType)) { 
                return EntityType.ORG;
            } else { //Special cases
                if(infoboxType.equals("musical artist")) {
                    Optional<String> background = text.getSingleMetadata("ib_Background");
                    if(background.isPresent()) {
                        if(personArtists.contains(background.get())) {
                            return EntityType.PERSON;
                        } else if (orgArtists.contains("group_or_band")){
                            return EntityType.ORG;
                        } else {
                            System.out.println(background.get());
                            throw new RuntimeException("abcdefg");
                        }
                    }
                }
            }
            
            return EntityType.UNK;
        }
    }
    
}
