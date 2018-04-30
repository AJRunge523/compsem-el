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
    
    static Set<String> gpeProps;
    static Set<String> personProps;
    static Set<String> orgProps;
    
    static {
        personTypes = new HashSet<>();
        personTypes.add("writer");
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
        personTypes.add("chess player");
        personTypes.add("nflactive");
        personTypes.add("senator");
        personTypes.add("character");
        personTypes.add("_congressman");
        personTypes.add("_actor");
        personTypes.add("_vice president");
        personTypes.add("radio presenter");
        personTypes.add("chef");
        personTypes.add("rugby league biography");
        personTypes.add("bishopbiog");
        personTypes.add("cricketer");
        personTypes.add("state senator");
        personTypes.add("actor voice");
        personTypes.add("pro football player");
        personTypes.add("nfl player");
        personTypes.add("ice hockey player");
        personTypes.add("_philosopher");
        personTypes.add("euroleague player");
        personTypes.add("chinese-language singer");
        personTypes.add("old cricketer");
        personTypes.add("architect");
        personTypes.add("badminton player");
        personTypes.add("former grand prix motorcycle rider");
        personTypes.add("speedway rider");
        
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
        gpeTypes.add("england county");
        gpeTypes.add("autonomous region of china (prc)");
        gpeTypes.add("uk place");
        gpeTypes.add("city in afghanistan");
        gpeTypes.add("cityit");
        gpeTypes.add("australian place");
        gpeTypes.add("city");
        gpeTypes.add("korean settlement");
        gpeTypes.add("belgium municipality");
        gpeTypes.add("canada electoral district");
        gpeTypes.add("u.s. congressional district");
        gpeTypes.add("german location");
        gpeTypes.add("school district");
        gpeTypes.add("philippine municipality");
        
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
        orgTypes.add("co-operative");
        orgTypes.add("organization");
        orgTypes.add("militant organization");
        orgTypes.add("law enforcement agency");
        orgTypes.add("american_political_party");
        orgTypes.add("_organization");
        orgTypes.add("football club");
        orgTypes.add("prc political parties");
        orgTypes.add("airport");
        orgTypes.add("exchange");
        orgTypes.add("firedepartment");
        orgTypes.add("_airline");
        orgTypes.add("un");
        orgTypes.add("radio station");
        orgTypes.add("central bank");
        orgTypes.add("bus transit");
        orgTypes.add("software");
        orgTypes.add("_software");
        orgTypes.add("defunct company");
        orgTypes.add("rail line");
        orgTypes.add("london bus");
        orgTypes.add("_radio station");
        orgTypes.add("_newspaper");
        orgTypes.add("national football team");
        orgTypes.add("brewery");
        orgTypes.add("school district");
        
        
        personArtists = new HashSet<>();
        personArtists.add("solo_singer");
        personArtists.add("non_vocal_instrumentalist");
        personArtists.add("Malaysian_actres");
        orgArtists = new HashSet<>();
        orgArtists.add("group_or_band");
        
        personProps = new HashSet<>();
        personProps.add("ib_dateofbirth");
        personProps.add("ib_placeofbirth");
        
        orgProps = new HashSet<>();
        orgProps.add("ib_members");
        orgProps.add("ib_disbanded");
        orgProps.add("ib_founded");
        orgProps.add("ib_owner");
        orgProps.add("ib_parentcompany");
        orgProps.add("ib_headquarters");
        
        gpeProps = new HashSet<>();
        gpeProps.add("ib_capital");
        gpeProps.add("ib_population");
        gpeProps.add("ib_populationasof");
        gpeProps.add("ib_foundation");
        gpeProps.add("ib_website");
        gpeProps.add("ib_coords");
        gpeProps.add("ib_coordinates");
        gpeProps.add("ib_elevation");
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
                            throw new RuntimeException("abcdefg");
                        }
                    }
                } else {
                    for(String ibProp : personProps) {
                        if(text.getSingleMetadata(ibProp).isPresent()) {
                            return EntityType.PERSON;
                        }
                    }
                    for(String ibProp : gpeProps) {
                        if(text.getSingleMetadata(ibProp).isPresent()) {
                            return EntityType.GPE;
                        }
                    }
                    for(String ibProp : orgProps) {
                        if(text.getSingleMetadata(ibProp).isPresent()) {
                            return EntityType.ORG;
                        }
                    }
                }
            }
            
            return EntityType.UNK;
        }
    }
    
}
