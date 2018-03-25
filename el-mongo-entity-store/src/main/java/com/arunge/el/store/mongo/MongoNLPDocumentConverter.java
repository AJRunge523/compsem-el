package com.arunge.el.store.mongo;

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
    
    public static NLPDocument toNLPDocument(Document doc) {
        String id = doc.getString("_id");
        NLPDocument d = new NLPDocument(id);
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
