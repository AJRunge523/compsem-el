package com.arunge.el.attribute.extraction;

import java.util.HashMap;
import java.util.Map;

import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.StringAttribute;

/**
 * 
 *<p>Extract gold labels from training queries to facilitate training/eval process.<p>
 *
 * @author Andrew Runge
 *
 */
public class GoldLabelExtractor implements AttributeExtractor {

    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        if(text.getMetadata().containsKey("gold")) {
            attributes.put(EntityAttribute.GOLD_LABEL, StringAttribute.valueOf(text.getSingleMetadata("gold").get()));
        }
        if(text.getMetadata().containsKey("goldNER")) {
            attributes.put(EntityAttribute.GOLD_NER, StringAttribute.valueOf(text.getSingleMetadata("goldNER").get()));
        }
        return attributes;
    }

    
    
}
