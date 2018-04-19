package com.arunge.el.store.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.NLPDocument;

public class MongoNLPDocumentConverter {

    public static Document toMongoDocument(NLPDocument doc) {
        Document d = new Document();
        d.append("_id", doc.getId());
        return d;
    }
    
    @SuppressWarnings("unchecked")
    public static NLPDocument toNLPDocument(Document doc) {
        String id = doc.getString("_id");
        NLPDocument d = new NLPDocument(id);
        if(doc.containsKey(MongoNLPFields.WIKI_ALIASES)) {
            ArrayList<String> aliases = (ArrayList<String>) doc.get(MongoNLPFields.WIKI_ALIASES);
            d.setAliases(aliases);
        }
        if(doc.containsKey(MongoNLPFields.INREF_ENTITIES)) { 
            ArrayList<String> aliases = (ArrayList<String>) doc.get(MongoNLPFields.INREF_ENTITIES);
            d.setInrefEntities(aliases);
        }
        if(doc.containsKey(MongoNLPFields.OUTREF_ENTITIES)) { 
            ArrayList<String> aliases = (ArrayList<String>) doc.get(MongoNLPFields.OUTREF_ENTITIES);
            d.setOutrefEntities(aliases);
        }
        if(doc.containsKey(MongoNLPFields.COREF_ENTITIES)) { 
            ArrayList<String> aliases = (ArrayList<String>) doc.get(MongoNLPFields.COREF_ENTITIES);
            d.setCorefEntities(aliases);
        }
        if(doc.containsKey(MongoNLPFields.NER_TYPE)) { 
            d.setEntityType(EntityType.valueOf(doc.getString(MongoNLPFields.NER_TYPE)));
        }
        for(ContextType type : ContextType.values()) {
            if(doc.containsKey(type.name())) { 
                if(doc.get(type.name()) instanceof Document) {
                    Document dist = (Document) doc.get(type.name());
                    Map<Integer, Double> distribution = new HashMap<>();
                    for(String key : dist.keySet()) {
                        distribution.put(Integer.parseInt(key), dist.getDouble(key));
                    }
                    d.addDistribution(type, distribution);
                } else if (doc.get(type.name()) instanceof ArrayList) {
                    ArrayList<Double> dist = (ArrayList<Double>) doc.get(type.name());
                    Map<Integer, Double> distribution = new HashMap<>();
                    for(int i = 0; i < dist.size(); i++) {
                        distribution.put(i, dist.get(i));
                    }
                    d.addDistribution(type, distribution);
                }
            }
        }
        return d;
    }
    
}
