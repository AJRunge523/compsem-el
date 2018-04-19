package com.arunge.el.attribute.extraction;

import java.util.HashMap;
import java.util.Map;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;

public class TopicModelExtractor implements AttributeExtractor {
    
    private ContextType[] types;
    
    public TopicModelExtractor() {
        this.types = new ContextType[] { ContextType.TOPIC_25, 
                ContextType.TOPIC_50, 
                ContextType.TOPIC_100, 
                ContextType.TOPIC_200, 
                ContextType.TOPIC_300, 
                ContextType.TOPIC_400, 
                ContextType.TOPIC_500 };
    }
    
    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        for(ContextType type : types) {
            if(nlp.getDistribution(type) != null) {
                EntityAttribute key = null;
                switch(type) {
                case TOPIC_25:
                    key = EntityAttribute.TOPIC_25;
                    break;
                case TOPIC_50:
                    key = EntityAttribute.TOPIC_50;
                    break;
                case TOPIC_100:
                    key = EntityAttribute.TOPIC_100;
                    break;
                case TOPIC_200:
                    key = EntityAttribute.TOPIC_200;
                    break;
                case TOPIC_300:
                    key = EntityAttribute.TOPIC_300;
                    break;
                case TOPIC_400:
                    key = EntityAttribute.TOPIC_400;
                    break;
                case TOPIC_500:
                    key = EntityAttribute.TOPIC_500;
                    break;
                default:
                    throw new RuntimeException("Illegal context type for a topic model: " + type.name());
                }
                attributes.put(key, new SparseVectorAttribute(nlp.getDistribution(type)));
            }
        }
        return attributes;
    }
}
