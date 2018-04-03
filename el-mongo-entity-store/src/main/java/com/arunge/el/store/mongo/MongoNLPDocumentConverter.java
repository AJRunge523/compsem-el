package com.arunge.el.store.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.arunge.el.api.ContextType;
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
        if(doc.containsKey(MongoNLPFields.WIKILINKS_ALIASES)) {
            ArrayList<String> aliases = (ArrayList<String>) doc.get(MongoNLPFields.WIKILINKS_ALIASES);
            d.setAliases(aliases);
        }
        for(ContextType type : ContextType.values()) {
            if(doc.containsKey(type.name())) { 
                Document dist = (Document) doc.get(type.name());
                Map<Integer, Double> distribution = new HashMap<>();
                for(String key : dist.keySet()) {
                    distribution.put(Integer.parseInt(key), dist.getDouble(key));
                }
                d.addDistribution(type, distribution);
            }
        }
        return d;
    }
    
}
