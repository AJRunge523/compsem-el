package com.arunge.el.attribute.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;
import com.arunge.el.attribute.Attribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.StringAttribute;
import com.arunge.nlp.api.Tokenizer;
import com.arunge.nlp.stanford.FilteredTokenizer;
import com.arunge.nlp.stanford.Tokenizers;
import com.arunge.nlp.tokenization.TokenFilters;
import com.arunge.nlp.tokenization.TokenFilters.TokenFilter;

public class AcronymExtractor implements AttributeExtractor {

    private Pattern p = Pattern.compile("[A-Z0-9]*");
    
    private Tokenizer tokenizer;
    
    public AcronymExtractor() {
        List<TokenFilter> filters = new ArrayList<>();
        filters.add(TokenFilters.stopwords());
        filters.add(TokenFilters.punctuation());
        filters.add(TokenFilters.maxLength(50));
        this.tokenizer = new FilteredTokenizer(Tokenizers.getDefault(), filters);
    }
    
    @Override
    public Map<EntityAttribute, Attribute> extract(TextEntity text, NLPDocument nlp) {
        Map<EntityAttribute, Attribute> attributes = new HashMap<>();
        String name = stripPunct(text.getName());
        String acronym = tokenizer.tokenize(name).map(t -> t.text()).map(w -> toAcronymStr(w)).reduce("", (a, b) -> a + b);
        if(acronym.length() > 1) {
            attributes.put(EntityAttribute.ACRONYM, StringAttribute.valueOf(acronym));            
        }
        
        return attributes;
    }

    private String stripPunct(String name) {
        String clean = name.replaceAll("\\(.*\\)", "");
        clean = clean.replaceAll("\\p{Punct}", "");
        return clean;
    }
    
    private String toAcronymStr(String word) {
        Matcher m = p.matcher(word);
        if(m.matches()) {
            return word;
        } else {
            return String.valueOf(word.charAt(0)).toUpperCase();
        }
    }
}
