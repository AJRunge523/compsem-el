package com.arunge.el.attribute.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SetAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.FilteredTokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.tokenization.TokenFilters;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;

/**
 * 
 *<p>Extractor for features related to names, aliases, and unigrams/bigrams of the names.<p>
 *
 * @author Andrew Runge
 *
 */
public class NameExtractor implements AttributeExtractor {

    private HashMap<String, String> replacements;
    
    private Tokenizer tokenizer;
    
    public NameExtractor() {
        List<TokenFilter> filters = new ArrayList<>();
        filters.add(TokenFilters.stopwords());
        filters.add(TokenFilters.punctuation());
        filters.add(TokenFilters.maxLength(50));
        this.tokenizer = new FilteredTokenizer(Tokenizers.getDefault(), filters);
        initReplacements();
    }

    private void initReplacements() {
        replacements = new HashMap<>();
        replacements.put("corp", "corporation");
        replacements.put("inst", "institute");
        replacements.put("llc", "");
        replacements.put("ltd", "");
        replacements.put("pllc", "");
        replacements.put("gmbh", "");
    }
    
    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        String name = text.getName();
        attributes.put(EntityAttribute.NAME, StringAttribute.valueOf(name));
        String cleansedName = cleanCanonicalName(text.getName(), true);
        attributes.put(EntityAttribute.CLEANSED_NAME, StringAttribute.valueOf(cleansedName.replaceAll(" ", "")));
        Set<String> aliases = nlp.getAliases();
//        aliases.add(name);
        attributes.put(EntityAttribute.ALIASES, SetAttribute.valueOf(aliases));
        Set<String> cleansedAliases = aliases.stream().map(a -> cleanCanonicalName(a, false)).collect(Collectors.toSet());
        attributes.put(EntityAttribute.CLEANSED_ALIASES, SetAttribute.valueOf(cleansedAliases));
        attributes.putAll(getNameNgrams(name, cleansedName, new HashSet<>()));
        return attributes;
    }

    private String cleanCanonicalName(String name, boolean useSpaces) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("-", " ");
        clean = clean.replaceAll("\\p{Punct}", "");
        clean = clean.toLowerCase();
        clean = tokenizer.tokenize(clean).map(t -> t.text()).map(t -> replaceToken(t)).reduce("", (a, b) -> a + (useSpaces ? " " : "") + b);
        return clean.trim();
    }
    
    private String replaceToken(String token) {
        if(replacements.containsKey(token)) {
            return replacements.get(token);
        }
        return token;
    }
    
    /**
     * Adds name ngram information to the provided entity. Modifies the entity in place.
     * @param entity
     */
    private HashMap<EntityAttribute, Attribute> getNameNgrams(String name, String cleansedName, Set<String> aliases) {
        List<String> names = new ArrayList<>();
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
