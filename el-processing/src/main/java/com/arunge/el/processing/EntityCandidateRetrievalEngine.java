package com.arunge.el.processing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.EntityStore;
import com.arunge.el.api.KBEntity;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.unmei.iterators.Iterators;

public class EntityCandidateRetrievalEngine {

    private EntityStore store;
    
    private Tokenizer tokenizer;
    
    public EntityCandidateRetrievalEngine(EntityStore store) { 
        this.store = store;
        this.tokenizer = Tokenizers.getDefaultFiltered();
    }
    
    public Stream<KBEntity> retrieveCandidates(KBEntity queryEntity) {
        EntityQuery.Builder queryBuilder = EntityQuery.builder();
        List<String> names = new ArrayList<>();
        names.add(queryEntity.getCanonicalName());
        for(String alias : queryEntity.getAliases()) {
            names.add(alias);
        }
        queryBuilder = queryBuilder.withNameVariants(names);
        Set<String> nameUnigrams = new HashSet<>();
        for(String name : names) {
            Stream<Token> tokens = tokenizer.tokenize(name);
            tokens.forEach(t -> nameUnigrams.add(t.text()));
        }
        queryBuilder.withNameUnigrams(nameUnigrams);
        return Iterators.toStream(store.query(queryBuilder.build()));
    }
    
}
