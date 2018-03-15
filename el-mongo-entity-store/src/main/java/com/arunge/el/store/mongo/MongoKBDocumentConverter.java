package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoKBFields.META;
import static com.arunge.el.store.mongo.MongoKBFields.NAME;
import static com.arunge.el.store.mongo.MongoKBFields.TEXT;
import static com.arunge.el.store.mongo.MongoKBFields.TYPE;

import java.util.ArrayList;
import java.util.Collection;

import org.bson.Document;

import com.arunge.el.api.TextEntity;
import com.mongodb.BasicDBObject;

public class MongoKBDocumentConverter {

    public static Document toMongoDocument(TextEntity doc) {
        Document document = new Document();
        document.append("_id", doc.getId());
        document.append(NAME, doc.getName());
        document.append(TYPE, doc.getEntityType());
        document.append(TEXT, doc.getDocText());
        BasicDBObject infoboxObj = new BasicDBObject();
        for(String key : doc.getMetadata().keySet()) {
            Collection<String> vals = doc.getMetadata(key);
            infoboxObj.append(key, vals);
        }
        document.append(META, infoboxObj);
        return document;
    }
    
    @SuppressWarnings("unchecked")
    public static TextEntity toTextEntity(Document doc) {
        String id = doc.getString("_id");
        String name = doc.getString(NAME);
        String type = doc.getString(TYPE);
        String text = doc.getString(TEXT);
        TextEntity entity = new TextEntity(id, text);
        entity.setName(name);
        entity.setEntityType(type);
        Document obj = (Document) doc.get(META);
        for(String key : obj.keySet()) {
            ArrayList<String> values = (ArrayList<String>) obj.get(key);
            for(String v : values) {
                entity.putMetadata(key, v);
            }
        }
        return entity;
    }
    
}
