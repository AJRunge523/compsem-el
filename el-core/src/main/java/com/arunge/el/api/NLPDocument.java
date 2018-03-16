package com.arunge.el.api;

import java.util.List;

import com.arunge.nlp.api.AnnotatedToken;

public class NLPDocument {

    private String id;
        /* List of sentences containing annotated tokens */
    private List<List<AnnotatedToken>> tokens;
    
    public NLPDocument(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<List<AnnotatedToken>> getTokens() {
        return tokens;
    }

    public void setTokens(List<List<AnnotatedToken>> tokens) {
        this.tokens = tokens;
    }

}
