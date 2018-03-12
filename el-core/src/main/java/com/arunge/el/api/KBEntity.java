package com.arunge.el.api;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 *<p>Class representing an entity from the knowledge base. This class will contain all relevant information from the 
 *   original infobox document necessary to perform entity linking.<p>
 *
 * @author Andrew Runge
 *
 */
public class KBEntity {

    private String id;
    
    private String kbName;
    
    private String canonicalName;
    
    private String[] aliases;
    
    private EntityType type;
    
    private Set<String> nameUnigrams;
    
    private Set<String> nameBigrams;

    public KBEntity() {
        this.aliases = new String[0];
        this.nameUnigrams = new HashSet<>();
        this.nameBigrams = new HashSet<>();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKbName() {
        return kbName;
    }

    public void setKbName(String kbName) {
        this.kbName = kbName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public Set<String> getNameUnigrams() {
        return nameUnigrams;
    }

    public void setNameUnigrams(Set<String> nameUnigrams) {
        this.nameUnigrams = nameUnigrams;
    }

    public Set<String> getNameBigrams() {
        return nameBigrams;
    }

    public void setNameBigrams(Set<String> nameBigrams) {
        this.nameBigrams = nameBigrams;
    }
    
}
