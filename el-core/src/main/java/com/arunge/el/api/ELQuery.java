package com.arunge.el.api;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

public class ELQuery {

    private String queryId;
    private String name;
    private String docPath;
    private CharSource docContent;
    private String goldEntity;
    
    
    public ELQuery() { }
    
    public ELQuery(String queryId, String name, String docPath, String goldEntity) {
        this.queryId = queryId;
        this.name = name;
        this.docPath = docPath;
        this.goldEntity = goldEntity;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public CharSource getDocContent() {
        if(docContent == null) {
            docContent = Files.asByteSource(new File(docPath)).asCharSource(Charsets.UTF_8);
        }
        return docContent;
    }

    public String getGoldEntity() {
        return goldEntity;
    }

    public void setGoldEntity(String goldEntity) {
        this.goldEntity = goldEntity;
    }
    
    public TextEntity convertToEntity() {
        try {
            TextEntity te = new TextEntity(queryId);
            te.setName(name);
            te.setDocText(getDocContent().read());
            te.putMetadata("gold", goldEntity);
            return te;
        } catch (IOException e) {
            throw new RuntimeException("Error reading document content from file.", e);
        }
    }
    
}
