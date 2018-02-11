package com.arunge.el.api;

import com.google.common.collect.Multimap;

public class KBDocument {

    private String id;
    private String entityType;
    private String name;
    private Multimap<String, InfoboxValue> infobox;
    private String infoboxType;
    private String docText;
    
    public KBDocument(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Multimap<String, InfoboxValue> getInfobox() {
        return infobox;
    }

    public void setInfobox(Multimap<String, InfoboxValue> infobox) {
        this.infobox = infobox;
    }

    public String getInfoboxType() {
        return infoboxType;
    }

    public void setInfoboxType(String infoboxType) {
        this.infoboxType = infoboxType;
    }

    public String getDocText() {
        return docText;
    }

    public void setDocText(String docText) {
        this.docText = docText;
    }
    
}
