package com.arunge.el.attribute.extraction;

import java.util.HashMap;
import java.util.Map;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.DoubleAttribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;

public class CorefEntityExtractor implements AttributeExtractor {

    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        if(nlp.getInrefEntities() != null && nlp.getInrefEntities().size() > 0) {
            attributes.put(EntityAttribute.INREF_ENTITIES, SetAttribute.valueOf(nlp.getInrefEntities()));
        }
        if(nlp.getOutrefEntities() != null && nlp.getOutrefEntities().size() > 0) {
            attributes.put(EntityAttribute.OUTREF_ENTITIES, SetAttribute.valueOf(nlp.getOutrefEntities()));
        }
        if(nlp.getCorefEntities() != null && nlp.getCorefEntities().size() > 0) {
            attributes.put(EntityAttribute.COREF_ENTITIES, SetAttribute.valueOf(nlp.getCorefEntities()));
        }
        attributes.put(EntityAttribute.OUT_LINKS, new DoubleAttribute(nlp.getOutLinks()));
        attributes.put(EntityAttribute.IN_LINKS, new DoubleAttribute(nlp.getInLinks()));
        if(nlp.getDistribution(ContextType.ENT_IB_DIST) != null) {
            attributes.put(EntityAttribute.ENT_IB_DIST, SparseVectorAttribute.valueOf(nlp.getDistribution(ContextType.ENT_IB_DIST)));
        }
        if(nlp.getDistribution(ContextType.ENT_CN_IB_DIST) != null) { 
            attributes.put(EntityAttribute.ENT_CN_IB_DIST, SparseVectorAttribute.valueOf(nlp.getDistribution(ContextType.ENT_CN_IB_DIST)));
        }
        return attributes;
    }

}
