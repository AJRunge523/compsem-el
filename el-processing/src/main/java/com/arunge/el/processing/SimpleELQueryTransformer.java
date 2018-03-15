package com.arunge.el.processing;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.KBEntity;
import com.arunge.nlp.api.Token;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.Tokenizers;

public class SimpleELQueryTransformer implements ELQueryTransformer {

    private Tokenizer tokenizer;
    
    public SimpleELQueryTransformer() {
        this.tokenizer = Tokenizers.getDefaultFiltered();

    }
    
    public void transform(KBEntity entity, ELQuery query) {
        entity.setName(query.getName());
        String cleansedName = cleanCanonicalName(query.getName());
        entity.setCleansedName(cleansedName);
        Set<String> nameUnigrams = new HashSet<>();
        Stream<Token> tokens = tokenizer.tokenize(cleansedName);
        tokens.forEach(t -> nameUnigrams.add(t.text()));
        entity.setNameUnigrams(nameUnigrams);
        entity.addMeta("gold", query.getGoldEntity());
    }
    
    private String cleanCanonicalName(String name) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("\\p{Punct}", "");
        clean = clean.toLowerCase();
        return clean;
    }
}
