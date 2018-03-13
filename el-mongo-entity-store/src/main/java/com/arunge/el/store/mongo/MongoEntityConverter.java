package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoEntityFields.ALIASES;
import static com.arunge.el.store.mongo.MongoEntityFields.CANONICAL_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.ID;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_BIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.NAME_UNIGRAMS;
import static com.arunge.el.store.mongo.MongoEntityFields.TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.bson.Document;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;

public class MongoEntityConverter {

    public static Document toMongoDocument(KBEntity e) {
        Document document = new Document();
        document.append(ID, e.getId());
        document.append(KB_NAME, e.getKbName());
        document.append(CANONICAL_NAME, e.getCanonicalName());
        document.append(ALIASES, Arrays.stream(e.getAliases()).collect(Collectors.toList()));
        document.append(TYPE, e.getType().name().charAt(0));
        document.append(NAME_UNIGRAMS, e.getNameUnigrams());
        document.append(NAME_BIGRAMS, e.getNameBigrams());
        return document;
    }
    
    @SuppressWarnings("unchecked")
    public static KBEntity toEntity(Document d) {
        KBEntity e = new KBEntity();
        e.setId(d.getString(ID));
        e.setKbName(d.getString(KB_NAME));
        e.setCanonicalName(d.getString(CANONICAL_NAME));
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
        e.setNameUnigrams(new HashSet<>(unigrams));
        e.setNameBigrams(new HashSet<>(bigrams));
        return e;
    }
}
