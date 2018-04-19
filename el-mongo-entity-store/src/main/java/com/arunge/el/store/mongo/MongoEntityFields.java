package com.arunge.el.store.mongo;

import java.util.HashMap;
import java.util.Map;

import com.arunge.el.attribute.EntityAttribute;

public class MongoEntityFields {

    public static String ID = "_id";
    public static String KB_NAME = "kn";
    public static String CANONICAL_NAME = "cn";
    public static String ALIASES = "a";
    public static String CLEANSED_ALIASES = "ca";
    public static String TYPE = "y";
    public static String NAME_UNIGRAMS = "nu";
    public static String NAME_BIGRAMS = "nb";
    public static String ACRONYM = "acr";
    public static String CONTEXT = "cxt";
    public static String GOLD_LABEL = "gold";
    public static String GOLD_NER = "goldNER";
    public static String TOPIC25 = "t25";
    public static String TOPIC50 = "t50";
    public static String TOPIC100 = "t100";
    public static String TOPIC200 = "t200";
    public static String TOPIC300 = "t300";
    public static String TOPIC400 = "t400";
    public static String TOPIC500 = "t500";
    public static String INREF_ENTITIES = "inent";
    public static String OUTREF_ENTITIES = "outent";
    public static String COREF_ENTITIES = "coent";
    
    
    private static Map<String, EntityAttribute> attrLookup;
    private static Map<EntityAttribute, String> fieldLookup;
    
    static {
        attrLookup = new HashMap<>();
        attrLookup.put(KB_NAME, EntityAttribute.NAME);
        attrLookup.put(CANONICAL_NAME, EntityAttribute.CLEANSED_NAME);
        attrLookup.put(ALIASES, EntityAttribute.ALIASES);
        attrLookup.put(CLEANSED_ALIASES, EntityAttribute.CLEANSED_ALIASES);
        attrLookup.put(NAME_UNIGRAMS, EntityAttribute.UNIGRAMS);
        attrLookup.put(NAME_BIGRAMS, EntityAttribute.BIGRAMS);
        attrLookup.put(ACRONYM, EntityAttribute.ACRONYM);
        attrLookup.put(CONTEXT, EntityAttribute.CONTEXT_VECTOR);
        attrLookup.put(GOLD_LABEL, EntityAttribute.GOLD_LABEL);
        attrLookup.put(GOLD_NER, EntityAttribute.GOLD_NER);
        attrLookup.put(TOPIC25, EntityAttribute.TOPIC_25);
        attrLookup.put(TOPIC50, EntityAttribute.TOPIC_50);
        attrLookup.put(TOPIC100, EntityAttribute.TOPIC_100);
        attrLookup.put(TOPIC200, EntityAttribute.TOPIC_200);
        attrLookup.put(TOPIC300, EntityAttribute.TOPIC_300);
        attrLookup.put(TOPIC400, EntityAttribute.TOPIC_400);
        attrLookup.put(TOPIC500, EntityAttribute.TOPIC_500);
        attrLookup.put(TYPE, EntityAttribute.ENTITY_TYPE);
        attrLookup.put(INREF_ENTITIES, EntityAttribute.INREF_ENTITIES);
        attrLookup.put(OUTREF_ENTITIES, EntityAttribute.OUTREF_ENTITIES);
        attrLookup.put(COREF_ENTITIES, EntityAttribute.COREF_ENTITIES);
        
        fieldLookup = new HashMap<>();
        fieldLookup.put(EntityAttribute.NAME, KB_NAME);
        fieldLookup.put(EntityAttribute.CLEANSED_NAME, CANONICAL_NAME);
        fieldLookup.put(EntityAttribute.ALIASES, ALIASES);
        fieldLookup.put(EntityAttribute.CLEANSED_ALIASES, CLEANSED_ALIASES);
        fieldLookup.put(EntityAttribute.UNIGRAMS, NAME_UNIGRAMS);
        fieldLookup.put(EntityAttribute.BIGRAMS, NAME_BIGRAMS);
        fieldLookup.put(EntityAttribute.ACRONYM, ACRONYM);
        fieldLookup.put(EntityAttribute.CONTEXT_VECTOR, CONTEXT);
        fieldLookup.put(EntityAttribute.GOLD_LABEL, GOLD_LABEL);
        fieldLookup.put(EntityAttribute.GOLD_NER, GOLD_NER);
        fieldLookup.put(EntityAttribute.TOPIC_25, TOPIC25);
        fieldLookup.put(EntityAttribute.TOPIC_50, TOPIC50);
        fieldLookup.put(EntityAttribute.TOPIC_100, TOPIC100);
        fieldLookup.put(EntityAttribute.TOPIC_200, TOPIC200);
        fieldLookup.put(EntityAttribute.TOPIC_300, TOPIC300);
        fieldLookup.put(EntityAttribute.TOPIC_400, TOPIC400);
        fieldLookup.put(EntityAttribute.TOPIC_500, TOPIC500);
        fieldLookup.put(EntityAttribute.ENTITY_TYPE, TYPE);
        fieldLookup.put(EntityAttribute.INREF_ENTITIES, INREF_ENTITIES);
        fieldLookup.put(EntityAttribute.OUTREF_ENTITIES, OUTREF_ENTITIES);
        fieldLookup.put(EntityAttribute.COREF_ENTITIES, COREF_ENTITIES);
        
    }
    
    public static EntityAttribute toEntityAttribute(String field) { 
        if(attrLookup.containsKey(field)) {
            return attrLookup.get(field);
        }
        return null;
    }
    
    public static String toField(EntityAttribute attr) {
        if(fieldLookup.containsKey(attr)) {
            return fieldLookup.get(attr);
        }
        return null;
    }
    
    
    
}
