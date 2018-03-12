package com.arunge.el.processing;

import java.util.List;

import com.arunge.el.api.EntityType;
import com.arunge.nlp.api.AnnotatedToken;

public class NLPKBDocument {

    private String docId;
    private EntityType type;
    private String title;
        /* List of sentences containing annotated tokens */
    private List<List<AnnotatedToken>> tokens;
    
    public NLPKBDocument(String docId) {
        this.docId = docId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public List<List<AnnotatedToken>> getTokens() {
        return tokens;
    }

    public void setTokens(List<List<AnnotatedToken>> tokens) {
        this.tokens = tokens;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    
    
}
