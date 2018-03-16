package com.arunge.el.api;

import java.util.Collection;
import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * 
 *<p>A description of an entity backed by raw document text with associated metadata.<p>
 *
 * @author Andrew Runge
 *
 */
public class TextEntity {

    private String id;
    private Multimap<String, String> meta;
    private String text;
    
    public TextEntity(String id) {
        this(id, "");
    }
    
    public TextEntity(String id, String text) {
        this.id = id;
        this.text = text;
        this.meta = HashMultimap.create();
        setEntityType("UNK");
        setName("");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityType() {
        return getSingleMetadata(EntityMetadataKeys.ENTITY_TYPE).get();
    }

    public void setEntityType(String entityType) {
        clearMetadata(EntityMetadataKeys.ENTITY_TYPE);
        putMetadata(EntityMetadataKeys.ENTITY_TYPE, entityType);
    }

    public String getName() {
        return getSingleMetadata(EntityMetadataKeys.NAME).get();
    }

    public void setName(String name) {
        clearMetadata(EntityMetadataKeys.NAME);
        putMetadata(EntityMetadataKeys.NAME, name);
    }

    public Multimap<String, String> getMetadata() {
        return meta;
    }

    public void putMetadata(String key, String value) {
        this.meta.put(key, value);
    }
    
    public Collection<String> getMetadata(String key) {
        return meta.get(key);
    }
    
    public Optional<String> getSingleMetadata(String key) {
        Collection<String> values = meta.get(key);
        if(values == null || values.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(values.iterator().next());
        }
    }
    
    public void clearMetadata(String key) {
        meta.removeAll(key);
    }
    
    public String getDocText() {
        return text;
    }

    public void setDocText(String docText) {
        this.text = docText;
    }
    
}
