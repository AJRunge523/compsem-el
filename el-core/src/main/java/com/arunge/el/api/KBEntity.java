package com.arunge.el.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.StringAttribute;

/**
 * 
 *<p>Class representing an entity from the knowledge base. This class will contain all relevant information from the 
 *   original infobox document necessary to perform entity linking.<p>
 *
 * @author Andrew Runge
 *
 */
public class KBEntity {

    public enum EntityAttribute {
        NAME,
        CLEANSED_NAME,
        ALIASES,
        UNIGRAMS,
        BIGRAMS
    }
    
    private String id;
    
    private EntityType type;
    
    private Map<EntityAttribute, Attribute> attributes;
    
    private Map<String, String> metadata;
    
    public KBEntity() {
        this.attributes = new HashMap<>();
        this.metadata = new HashMap<>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Attribute getAttribute(EntityAttribute attr){ 
        return attributes.get(attr);
    }
    
    public String getName() {
        Attribute val = attributes.get(EntityAttribute.NAME);
        return val.getValueAsStr();
    }
    
    public void setName(String name) {
        this.attributes.put(EntityAttribute.NAME, new StringAttribute(name));
    }

    public String getCleansedName() {
        Attribute val = attributes.get(EntityAttribute.CLEANSED_NAME);
        return val.getValueAsStr();
    }

    public void setCleansedName(String cleansedName) {
        this.attributes.put(EntityAttribute.CLEANSED_NAME, new StringAttribute(cleansedName));
    }

    public Optional<Set<String>> getAliases() {
        SetAttribute attr = (SetAttribute) attributes.get(EntityAttribute.ALIASES);
        if(attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr.getSetValue());
    }

    public void setAliases(String[] aliases) {
        this.attributes.put(EntityAttribute.ALIASES, new SetAttribute(aliases));
    }
    
    public void setAliases(Collection<String> aliases) {
        this.attributes.put(EntityAttribute.ALIASES, new SetAttribute(aliases));
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public Optional<Set<String>> getNameUnigrams() {
        SetAttribute attr = (SetAttribute) attributes.get(EntityAttribute.UNIGRAMS);
        if(attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr.getSetValue());
    }

    public void setNameUnigrams(Collection<String> nameUnigrams) {
        this.attributes.put(EntityAttribute.UNIGRAMS, new SetAttribute(nameUnigrams));
    }

    public void setNameUnigrams(String[] nameUnigrams)  {
        this.attributes.put(EntityAttribute.UNIGRAMS, new SetAttribute(nameUnigrams));
    }
    
    public Optional<Set<String>> getNameBigrams() {
        SetAttribute attr = (SetAttribute) attributes.get(EntityAttribute.BIGRAMS);
        if(attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr.getSetValue());
    }

    public void setNameBigrams(Collection<String> nameBigrams) {
        this.attributes.put(EntityAttribute.BIGRAMS, new SetAttribute(nameBigrams));
    }
    
    public void setNameBigrams(String[] nameBigrams)  {
        this.attributes.put(EntityAttribute.BIGRAMS, new SetAttribute(nameBigrams));
    }
    
    public void addMeta(String key, String value) {
        this.metadata.put(key, value);
    }
    
    public Optional<String> getMeta(String key) {
        return Optional.ofNullable(this.metadata.get(key));
    }
    
}
