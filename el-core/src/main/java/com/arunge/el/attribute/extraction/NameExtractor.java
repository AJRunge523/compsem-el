package com.arunge.el.attribute.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arunge.el.api.EntityAttribute;
import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.TokenFilters;
import com.arunge.nlp.api.TokenFilters.TokenFilter;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.FilteredTokenizer;
import com.arunge.nlp.stanford.Tokenizers;

/**
 * 
 *<p>Extractor for features related to names and unigrams/bigrams of the names.<p>
 *
 * @author Andrew Runge
 *
 */
public class NameExtractor implements AttributeExtractor {

    private Tokenizer tokenizer;
    
    public NameExtractor() {
        List<TokenFilter> filters = new ArrayList<>();
        filters.add(TokenFilters.stopwords());
        filters.add(TokenFilters.punctuation());
        filters.add(TokenFilters.maxLength(50));
        this.tokenizer = new FilteredTokenizer(Tokenizers.getDefault(), filters);
    }
    
    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        String name = text.getName();
        attributes.put(EntityAttribute.NAME, StringAttribute.valueOf(name));
        String cleansedName = cleanCanonicalName(text.getName());
        attributes.put(EntityAttribute.CLEANSED_NAME, StringAttribute.valueOf(cleansedName));
        attributes.putAll(getNameNgrams(name, cleansedName, new HashSet<>()));
        return attributes;
    }

    private String cleanCanonicalName(String name) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("\\p{Punct}", "");
        clean = clean.toLowerCase();
        return clean;
    }
    
    /**
     * Adds name ngram information to the provided entity. Modifies the entity in place.
     * @param entity
     */
    private HashMap<EntityAttribute, Attribute> getNameNgrams(String name, String cleansedName, Set<String> aliases) {
        List<String> names = new ArrayList<>();
        names.add(name);
        names.add(cleansedName);
        names.addAll(aliases);
        Set<String> unigrams = new HashSet<>();
        Set<String> bigrams = new HashSet<>();
        for(String n : names) {
            n = n.toLowerCase();
            String[] words = tokenizer.tokenize(n).map(t -> t.text()).toArray(String[]::new);
            if(words.length == 0) { 
                continue;
            }
            unigrams.add(words[0]);
            for(int i = 1; i < words.length; i++) {
                unigrams.add(words[i]);
                bigrams.add(words[i-1] + " " + words[i]);
            }
        }
        HashMap<EntityAttribute, Attribute> attrs = new HashMap<>();
        attrs.put(EntityAttribute.UNIGRAMS, SetAttribute.valueOf(unigrams));
        attrs.put(EntityAttribute.BIGRAMS, SetAttribute.valueOf(bigrams));
        return attrs;
    }
}
