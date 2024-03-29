package com.arunge.el.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.extraction.AcronymExtractor;
import com.arunge.el.attribute.extraction.AttributeExtractor;
import com.arunge.el.attribute.extraction.CorefEntityExtractor;
import com.arunge.el.attribute.extraction.DistributionalContextExtractor;
import com.arunge.el.attribute.extraction.EntityTypeConverter;
import com.arunge.el.attribute.extraction.GoldLabelExtractor;
import com.arunge.el.attribute.extraction.NameExtractor;
import com.arunge.el.attribute.extraction.TopicModelExtractor;
import com.arunge.unmei.iterators.Iterators;
import com.google.common.collect.Streams;

/**
 * 
 *<p>Responsible for combining the information from the original text entity and the NLP-processed entity
 *   to create a single, cohesive representation for use in entity linking. <p>
 *
 * @author Andrew Runge
 *
 */
public class KBEntityConverter {

    private static Logger LOG = LoggerFactory.getLogger(KBEntityConverter.class);
    
    private int processed = 0;
    
    private List<AttributeExtractor> attrExtractors;
    
    private boolean useGoldNER;
    
    public KBEntityConverter(boolean useGoldNER) {
        this.attrExtractors = new ArrayList<>();
        this.attrExtractors.add(new NameExtractor());
        this.attrExtractors.add(new AcronymExtractor());
        this.attrExtractors.add(new DistributionalContextExtractor(ContextType.NORM_TFIDF));
        this.attrExtractors.add(new TopicModelExtractor());
        this.attrExtractors.add(new CorefEntityExtractor());
        this.attrExtractors.add(new GoldLabelExtractor());
        this.useGoldNER = useGoldNER;
    }
    
    /**
     * Process all entities in an {@link EntityKBStore} and stores the resulting <code>KBEntity</code>s in the store..
     * @param entityStore
     */
    public void process(EntityKBStore entityStore) {
        Stream<Pair<TextEntity, NLPDocument>> kbIter = Streams.zip(Iterators.toStream(entityStore.allKBText()), Iterators.toStream(entityStore.allNLPDocuments()), (a, b) -> Pair.of(a, b));
        kbIter.map(pair -> convert(pair.getLeft(), pair.getRight()))
              .forEach(e -> {
                  processed += 1;
                  entityStore.insert(e);
                  if(processed % 10000 == 0) {
                      LOG.info("Processed {} documents", processed);
                  }
              });
        
    }
    
    /**
     * Uses the information from the text and NLP-processed representations of an entity
     * to fill in the attributes of a {@link KBEntity} and returns it.
     * @param doc
     * @return
     */
    public KBEntity convert(TextEntity text, NLPDocument nlp) {
        KBEntity entity = new KBEntity(text.getId());
        EntityType eType = EntityTypeConverter.convert(text);
        if(useGoldNER) { 
            String goldNER = text.getSingleMetadata("goldNER").get();
            if(goldNER.equals("PER")) {
                eType = EntityType.PERSON;
            } else {
                eType = EntityType.valueOf(goldNER);
            }
        }
        if(eType.equals(EntityType.UNK) && nlp.getEntityType() != null) {
            eType = nlp.getEntityType();
        }
        entity.setType(eType);
        for(AttributeExtractor extractor : attrExtractors) {
            Map<EntityAttribute, Attribute> attributes = extractor.extract(text, nlp);
            for(EntityAttribute e : attributes.keySet()) {
                if(e == null) {
                    throw new RuntimeException("asdfljkaskldjfls");
                }
                entity.setAttribute(e, attributes.get(e));
            }
        }
        if(text.getMetadata().containsKey("gold")) { 
            entity.addMeta("gold", text.getSingleMetadata("gold").get());
        }
        return entity;
    }
    
}
