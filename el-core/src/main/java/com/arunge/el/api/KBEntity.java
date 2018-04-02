package com.arunge.el.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
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

    private String id;
    
    private EntityType type;
    
    private Map<EntityAttribute, Attribute> attributes;
    
    private Map<String, String> metadata;
    
    public KBEntity(String id) {
        this.id = id;
        this.attributes = new HashMap<>();
        this.metadata = new HashMap<>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public boolean hasAttribute(EntityAttribute attr) {
        return attributes.containsKey(attr);
    }
    
    public Attribute getAttribute(EntityAttribute attr){ 
        return attributes.get(attr);
    }
    
    public void setAttribute(EntityAttribute key, Attribute val) {
        this.attributes.put(key, val);
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
        this.attributes.put(EntityAttribute.ALIASES, SetAttribute.valueOf(aliases));
    }
    
    public void setAliases(Collection<String> aliases) {
        this.attributes.put(EntityAttribute.ALIASES, SetAttribute.valueOf(aliases));
    }

    public Optional<Set<String>> getNameUnigrams() {
        SetAttribute attr = (SetAttribute) attributes.get(EntityAttribute.UNIGRAMS);
        if(attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr.getSetValue());
    }

    public void setNameUnigrams(Collection<String> nameUnigrams) {
        this.attributes.put(EntityAttribute.UNIGRAMS, SetAttribute.valueOf(nameUnigrams));
    }

    public void setNameUnigrams(String[] nameUnigrams)  {
        this.attributes.put(EntityAttribute.UNIGRAMS, SetAttribute.valueOf(nameUnigrams));
    }
    
    public Optional<Set<String>> getNameBigrams() {
        SetAttribute attr = (SetAttribute) attributes.get(EntityAttribute.BIGRAMS);
        if(attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr.getSetValue());
    }

    public void setNameBigrams(Collection<String> nameBigrams) {
        this.attributes.put(EntityAttribute.BIGRAMS, SetAttribute.valueOf(nameBigrams));
    }
    
    public void setNameBigrams(String[] nameBigrams)  {
        this.attributes.put(EntityAttribute.BIGRAMS, SetAttribute.valueOf(nameBigrams));
    }
    
    public Optional<String> getAcronym() {
        Attribute val = attributes.get(EntityAttribute.ACRONYM);
        if(val == null) {
            return Optional.empty();
        }
        return Optional.of(val.getValueAsStr());
    }
    
    public void setAcronym(String acronym) {
        this.attributes.put(EntityAttribute.ACRONYM, StringAttribute.valueOf(acronym));
    }
    
    public void addMeta(String key, String value) {
        this.metadata.put(key, value);
    }
    
    public Optional<String> getMeta(String key) {
        return Optional.ofNullable(this.metadata.get(key));
    }
    
    public Optional<Map<Integer, Double>> getContext() {
        SparseVectorAttribute attr = (SparseVectorAttribute) attributes.get(EntityAttribute.CONTEXT_VECTOR);
        if(attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr.getValue());
    }
    
    public void setContext(Map<Integer, Double> context) {
        this.attributes.put(EntityAttribute.CONTEXT_VECTOR, new SparseVectorAttribute(context));
    }
    
}
