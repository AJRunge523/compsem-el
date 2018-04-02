package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoEntityFields.ACRONYM;
import static com.arunge.el.store.mongo.MongoEntityFields.ALIASES;
import static com.arunge.el.store.mongo.MongoEntityFields.CANONICAL_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.CONTEXT;
import static com.arunge.el.store.mongo.MongoEntityFields.GOLD_LABEL;
import static com.arunge.el.store.mongo.MongoEntityFields.ID;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_BIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_UNIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bson.Document;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.StringAttribute;

public class MongoEntityConverter {

    public static Document toMongoDocument(KBEntity e) {
        Document document = new Document();
        document.append(ID, e.getId());
        document.append(KB_NAME, e.getName());
        document.append(CANONICAL_NAME, e.getCleansedName());
        document.append(ALIASES, e.getAliases().orElse(new HashSet<>()));
        document.append(TYPE, e.getType().name().charAt(0));
        document.append(NAME_UNIGRAMS, e.getNameUnigrams().orElse(new HashSet<>()));
        document.append(NAME_BIGRAMS, e.getNameBigrams().orElse(new HashSet<>()));
        document.append(ACRONYM, e.getAcronym().orElse(""));
        document.append(CONTEXT, convertMap(e.getContext().orElse(new HashMap<>())));
        if(e.getAttribute(EntityAttribute.GOLD_LABEL) != null){ 
            document.append(GOLD_LABEL, e.getAttribute(EntityAttribute.GOLD_LABEL).getValueAsStr());
        }
        return document;
    }
    
    private static Document convertMap(Map<Integer, Double> vector) {
        Document valueDoc = new Document();
        for(Integer key : vector.keySet()) {
            valueDoc.append(key.toString(), vector.get(key));
        }
        return valueDoc;
    }
    
    private static Map<Integer, Double> convertVector(Document vecDoc) {
        Map<Integer, Double> vector = new HashMap<>();
        for(String key : vecDoc.keySet()) {
            vector.put(Integer.parseInt(key), vecDoc.getDouble(key));
        }
        return vector;
    }
    
    @SuppressWarnings("unchecked")
    public static KBEntity toEntity(Document d) {
        KBEntity e = new KBEntity(d.getString(ID));
        e.setName(d.getString(KB_NAME));
        e.setCleansedName(d.getString(CANONICAL_NAME));
        ArrayList<String> aliases = (ArrayList<String>) d.get(ALIASES);
        e.setAliases(aliases.stream().toArray(String[]::new));
        String typeStart = d.getString(TYPE);
        if(typeStart.equals("P")) {
            e.setType(EntityType.PERSON);
        } else if(typeStart.equals("G")) {
            e.setType(EntityType.GPE);
        } else if(typeStart.equals("O")) {
            e.setType(EntityType.ORG);
        } else {
            e.setType(EntityType.UNK);
        }
        ArrayList<String> unigrams = (ArrayList<String>) d.get(NAME_UNIGRAMS);
        ArrayList<String> bigrams = (ArrayList<String>) d.get(NAME_UNIGRAMS);
        Document vecDoc = (Document) d.get(CONTEXT);
        if(vecDoc != null) {
            Map<Integer, Double> vector = convertVector(vecDoc);
            e.setContext(vector);
        }
        if(d.getString(GOLD_LABEL) != null) {
            e.setAttribute(EntityAttribute.GOLD_LABEL, StringAttribute.valueOf(d.getString(GOLD_LABEL)));
        }
        e.setAcronym(d.getString(ACRONYM));
        e.setNameUnigrams(new HashSet<>(unigrams));
        e.setNameBigrams(new HashSet<>(bigrams));
        return e;
    }
}
