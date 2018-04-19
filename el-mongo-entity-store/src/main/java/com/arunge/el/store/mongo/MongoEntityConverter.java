package com.arunge.el.store.mongo;

import static com.arunge.el.store.mongo.MongoEntityFields.GOLD_LABEL;
import static com.arunge.el.store.mongo.MongoEntityFields.ID;
import static com.arunge.el.store.mongo.MongoEntityFields.KB_NAME;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.EntityAttributeTypes;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.google.common.collect.Sets;

public class MongoEntityConverter {

    public static Document toMongoDocument(KBEntity e) {
        Document document = new Document();
        document.append(ID, e.getId());
        
        for(EntityAttribute attr : e.getAttributes().keySet()) {
            String field = MongoEntityFields.toField(attr);
            if(field == null) { 
                System.out.println(e.getId());
                throw new RuntimeException("Unrecognized attribute: " + attr.name());
            }
            Attribute val = e.getAttribute(attr);
            if(val instanceof StringAttribute) {
                document.append(field, val.getValueAsStr());
            } else if(val instanceof SetAttribute) {
                SetAttribute setAttr = (SetAttribute) val;
                document.append(field, setAttr.getSetValue());
            } else if(val instanceof SparseVectorAttribute) {
                SparseVectorAttribute sva = (SparseVectorAttribute) val;
                document.append(field, convertMap(sva.getValue()));
            }
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
    
    public static KBEntity toEntity(Document d) {
        KBEntity e = new KBEntity(d.getString(ID));
        e.setName(d.getString(KB_NAME));
        for(String field : d.keySet()) {
            EntityAttribute attr = MongoEntityFields.toEntityAttribute(field);
            if(attr == null) {
                continue;
            }
            Class<? extends Attribute> attrClass = EntityAttributeTypes.getAttrType(attr);
            if(attrClass.equals(StringAttribute.class) || attrClass.equals(SetAttribute.class)) {
                e.setAttribute(attr, EntityAttributeTypes.wrapValue(attr, d.get(field)).get());
            } else if(attrClass.equals(SparseVectorAttribute.class)) {
                Map<Integer, Double> vector = convertVector((Document) d.get(field));
                e.setAttribute(attr, EntityAttributeTypes.wrapValue(attr, vector).get());
            } 
        }
        if(e.getAliases().isPresent()) {
            e.getAliases().get().add(e.getName());
        } else {
            e.setAliases(Sets.newHashSet(e.getName()));
        }
        if(e.getCleansedAliases().isPresent()) {
            e.getCleansedAliases().get().add(e.getCleansedName());
        } else {
            e.setAliases(Sets.newHashSet(e.getCleansedName()));
        }
        if(!e.getCleansedAliases().get().contains(e.getCleansedName())) {
            throw new RuntimeException("AAAAH");
        }
        if(!e.getAliases().get().contains(e.getName())) {
            throw new RuntimeException("AAAAH");
        }
        return e;
    }
}
