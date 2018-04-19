package com.arunge.el.attribute.extraction;

import java.util.Map;

import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;

public interface AttributeExtractor {

    Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp);
    
}
