package com.arunge.el.store.mongo;

import org.bson.Document;

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
        return d;
    }
    
}
