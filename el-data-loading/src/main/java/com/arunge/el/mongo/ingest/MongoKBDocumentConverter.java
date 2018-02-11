package com.arunge.el.mongo.ingest;

import static com.arunge.el.mongo.ingest.MongoKBFields.FACT_ENTITIES;
import static com.arunge.el.mongo.ingest.MongoKBFields.FACT_VALUE;
import static com.arunge.el.mongo.ingest.MongoKBFields.INFOBOX;
import static com.arunge.el.mongo.ingest.MongoKBFields.INFOBOX_TYPE;
import static com.arunge.el.mongo.ingest.MongoKBFields.NAME;
import static com.arunge.el.mongo.ingest.MongoKBFields.TEXT;
import static com.arunge.el.mongo.ingest.MongoKBFields.TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bson.Document;

import com.arunge.el.api.InfoboxValue;
import com.arunge.el.api.KBDocument;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoKBDocumentConverter {

    public static Document toMongoDocument(KBDocument doc) {
        Document document = new Document();
        document.append("_id", doc.getId());
        document.append(NAME, doc.getName());
        document.append(TYPE, doc.getEntityType());
        document.append(TEXT, doc.getDocText());
        document.append(INFOBOX_TYPE, doc.getInfoboxType());
        BasicDBObject infoboxObj = new BasicDBObject();
        for(String key : doc.getInfobox().keySet()) {
            List<DBObject> infoboxValues = new ArrayList<>();
            Collection<InfoboxValue> vals = doc.getInfobox().get(key);
            vals.stream().forEach(v -> infoboxValues.add(toDBObject(v)));
            infoboxObj.append(key, infoboxValues);
        }
        document.append(INFOBOX, infoboxObj);
        return document;
    }
    
    @SuppressWarnings("unchecked")
    public static KBDocument toKBDocument(Document doc) {
        String id = doc.getString("_id");
        String name = doc.getString(NAME);
        String type = doc.getString(TYPE);
        String text = doc.getString(TEXT);
        String infoboxType = doc.getString(INFOBOX_TYPE);
        Document obj = (Document) doc.get(INFOBOX);
        Multimap<String, InfoboxValue> infobox = HashMultimap.create();
        for(String key : obj.keySet()) {
            ArrayList<Document> values = (ArrayList<Document>) obj.get(key);
            values.stream().map(v -> toInfoboxValue(v))
                    .forEach(v -> infobox.put(key, v));
        }
        
        KBDocument kbDoc = new KBDocument(id);
        kbDoc.setName(name);
        kbDoc.setEntityType(type);
        kbDoc.setDocText(text);
        kbDoc.setInfoboxType(infoboxType);
        kbDoc.setInfobox(infobox);
        return kbDoc;
    }
    
    private static DBObject toDBObject(InfoboxValue val) {
        BasicDBObject obj = new BasicDBObject();
        obj.append(FACT_VALUE, val.getValue());
        obj.append(FACT_ENTITIES, val.getKbLink());
        return obj;
    }
    
    @SuppressWarnings("unchecked")
    private static InfoboxValue toInfoboxValue(Document obj) {
        String value = (String) obj.get(FACT_VALUE);
        ArrayList<String> entities = (ArrayList<String>) obj.get(FACT_ENTITIES);
        return new InfoboxValue(value, entities);
    }
    
}
