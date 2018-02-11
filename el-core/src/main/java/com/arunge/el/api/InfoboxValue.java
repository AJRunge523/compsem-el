package com.arunge.el.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InfoboxValue {

    private String value;
    private List<String> kbLinks;
    
    public InfoboxValue(String value) {
        this(value, new ArrayList<>());
    }
    
    public InfoboxValue(String value, Collection<String> kbLinks) {
        this.value = value;
        this.kbLinks = new ArrayList<>(kbLinks);
    }
    
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Collection<String> getKbLink() {
        return kbLinks;
    }
    public void setKbLinks(Collection<String> kbLink) {
        this.kbLinks = new ArrayList<>(kbLinks);
    }
    public void addKbLink(String kbLink) {
        this.kbLinks.add(kbLink);
    }
}
