package com.arunge.el.attribute.extraction;

import java.util.HashMap;
import java.util.Map;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;

public class DistributionalContextExtractor implements AttributeExtractor {

    private ContextType type;
    
    public DistributionalContextExtractor(ContextType type) {
        this.type = type;
    }
    
    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        if(nlp.getDistribution(type) != null) {
            attributes.put(EntityAttribute.CONTEXT_VECTOR, new SparseVectorAttribute(nlp.getDistribution(type)));
        }
        return attributes;
    }

}
