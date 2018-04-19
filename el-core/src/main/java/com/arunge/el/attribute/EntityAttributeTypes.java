package com.arunge.el.attribute;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class EntityAttributeTypes {

    public static Class<? extends Attribute> getAttrType(EntityAttribute attr) {
        switch(attr) {
        case ACRONYM:
            return StringAttribute.class;
        case ALIASES:
            return SetAttribute.class;
        case BIGRAMS:
            return SetAttribute.class;
        case CLEANSED_ALIASES:
            return SetAttribute.class;
        case CLEANSED_NAME:
            return StringAttribute.class;
        case CONTEXT_VECTOR:
            return SparseVectorAttribute.class;
        case GOLD_LABEL:
            return StringAttribute.class;
        case GOLD_NER:
            return StringAttribute.class;
        case METAPHONE:
            return StringAttribute.class;
        case NAME:
            return StringAttribute.class;
        case TOPIC_100:
            return SparseVectorAttribute.class;
        case TOPIC_200:
            return SparseVectorAttribute.class;
        case TOPIC_25:
            return SparseVectorAttribute.class;
        case TOPIC_300:
            return SparseVectorAttribute.class;
        case TOPIC_400:
            return SparseVectorAttribute.class;
        case TOPIC_50:
            return SparseVectorAttribute.class;
        case TOPIC_500:
            return SparseVectorAttribute.class;
        case UNIGRAMS:
            return SetAttribute.class;
        case COREF_ENTITIES:
            return SetAttribute.class;
        case ENTITY_TYPE:
            return StringAttribute.class;
        case INREF_ENTITIES:
            return SetAttribute.class;
        case OUTREF_ENTITIES:
            return SetAttribute.class;
        default:
            return null;
        }
    }
    
    /**
     * Attempts to wrap the value in the appropriate attribute type for the given entity attribute.
     * If the value is null or is the incorrect type, then this will return Optional.empty(). 
     * @param attr
     * @param val
     * @return
     */
    public static Optional<Attribute> wrapValue(EntityAttribute attr, Object val) {
        switch(attr) {
        case NAME:
            return wrapString(val);
        case ACRONYM:
            return wrapString(val);
        case ALIASES:
            return wrapSet(val);
        case BIGRAMS:
            return wrapSet(val);
        case CLEANSED_ALIASES:
            return wrapSet(val);
        case CLEANSED_NAME:
            return wrapString(val);
        case CONTEXT_VECTOR:
            return wrapVector(val);
        case GOLD_LABEL:
            return wrapString(val);
        case GOLD_NER:
            return wrapString(val);
        case METAPHONE:
            return wrapString(val);
        case TOPIC_100:
            return wrapVector(val);
        case TOPIC_200:
            return wrapVector(val);
        case TOPIC_25:
            return wrapVector(val);
        case TOPIC_300:
            return wrapVector(val);
        case TOPIC_400:
            return wrapVector(val);
        case TOPIC_50:
            return wrapVector(val);
        case TOPIC_500:
            return wrapVector(val);
        case UNIGRAMS:
            return wrapSet(val);
        case COREF_ENTITIES:
            return wrapSet(val);
        case ENTITY_TYPE:
            return wrapString(val);
        case INREF_ENTITIES:
            return wrapSet(val);
        case OUTREF_ENTITIES:
            return wrapSet(val);
        default:
            throw new RuntimeException("Unknown attribute: " + attr.name());
        }
    }
    
    private static Optional<Attribute> wrapString(Object val) {
        try {
            return Optional.of(StringAttribute.valueOf((String) val));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Optional<Attribute> wrapSet(Object val) {
        try {
            return Optional.of(SetAttribute.valueOf((Collection<String>) val));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Optional<Attribute> wrapVector(Object val) {
        try {
            return Optional.of(SparseVectorAttribute.valueOf((Map<Integer, Double>) val));
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }
    
}
