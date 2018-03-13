package com.arunge.el.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arunge.el.api.EntityStore;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBDocument;
import com.arunge.el.api.KBEntity;
import com.arunge.el.api.KnowledgeBaseStore;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.TokenFilters.TokenFilter;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.unmei.iterators.Iterators;

public class KBEntityProcessingPipeline {

    private static Logger LOG = LoggerFactory.getLogger(KBEntityProcessingPipeline.class);
    
    private KnowledgeBaseStore kb;
    private EntityStore entityStore;
    private int processed = 0;
    private Map<String, Integer> unkCounts;
    private Tokenizer tokenizer;
    private List<TokenFilter> filters;
//    private KBDocumentTextProcessor processor;
    
    
    public KBEntityProcessingPipeline(KnowledgeBaseStore kb, EntityStore entityStore) {
        this.kb = kb;
        this.entityStore = entityStore;
        this.unkCounts = new HashMap<>();
        this.tokenizer = Tokenizers.getDefaultFiltered();
//        this.processor = processor;
    }
    
    public void process() {
        Stream<KBDocument> kbIter = Iterators.toStream(kb.all());
        //TODO: Before conversion, these documents will be heavily processed to perform feature extraction
        kbIter.map(d -> process(d))
              .filter(d -> d.isPresent())
              .map(d -> convert(d.get()))
              .forEach(e -> {
                  processed += 1;
                  entityStore.insert(e);
                  if(processed % 10000 == 0) {
                      LOG.info("Processed {} documents", processed);
                  }
              });
        
//        for(String key : unkCounts.keySet()) {
//            System.out.println(key + " --> " + unkCounts.get(key));
//        }
    }
    
    /**
     * Processes a KBDocument using an NLP pipeline. Optionally returns a document, allowing processors
     * to determine whether the document should be filtered out.
     * @param doc
     * @return
     */
    private Optional<NLPKBDocument> process(KBDocument document) { 
        NLPKBDocument output = new NLPKBDocument(document.getId());
        EntityType type = parseType(document.getEntityType(), document.getInfoboxType());
        if(type.equals(EntityType.UNK)) {
            String ibType = document.getInfoboxType().toLowerCase();
            unkCounts.compute(ibType, (k, v) -> (v == null) ? 1 : v+1);
            return Optional.empty();
        }
        output.setType(type);
        output.setTitle(document.getName());
        return Optional.of(output);
    }
    
    private String cleanCanonicalName(String name) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("\\p{Punct}", "");
        return clean;
    }
    
    
    private KBEntity convert(NLPKBDocument doc) {
        KBEntity entity = new KBEntity();
        entity.setId(doc.getDocId());
        entity.setName(doc.getTitle());
        entity.setCleansedName(cleanCanonicalName(doc.getTitle()));
        entity.setType(doc.getType());
        addNameNgrams(entity);
        return entity;
    }
    
    /**
     * Adds name ngram information to the provided entity. Modifies the entity in place.
     * @param entity
     */
    private void addNameNgrams(KBEntity entity) {
        List<String> names = new ArrayList<>();
        names.add(entity.getCleansedName());
        names.add(entity.getName());
        Optional<Set<String>> aliases = entity.getAliases();
        if(aliases.isPresent()) { 
            names.addAll(aliases.get());
        }
        Set<String> unigrams = new HashSet<>();
        Set<String> bigrams = new HashSet<>();
        for(String name : names) {
            List<Token> tokens = tokenizer.tokenizeToList(name);
            if(tokens.size() == 0) {
                continue;
            }
            unigrams.add(tokens.get(0).text());
            for(int i = 1; i < tokens.size(); i++) {
                unigrams.add(tokens.get(i).text());
                bigrams.add(tokens.get(i - 1).text() + " " + tokens.get(i).text());
            }
        }
        entity.setNameUnigrams(unigrams);
        entity.setNameBigrams(bigrams);
    }
    
    private EntityType parseType(String eType, String ibType) {
        if(eType.toLowerCase().equals("per")) {
            return EntityType.PERSON;
        } else if(eType.toLowerCase().equals("gpe")) {
            return EntityType.GPE;
        } else if(eType.toLowerCase().equals("org")) {
            return EntityType.ORG;
        } else {
            return EntityType.UNK;
        }
    }
    
    
    
}
