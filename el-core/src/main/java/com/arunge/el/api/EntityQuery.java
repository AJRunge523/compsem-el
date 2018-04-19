package com.arunge.el.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityQuery {

    private List<String> rawNames;
    
    private List<String> cleansedNames;
    
    private List<String> nameUnigrams;
    
    private List<String> nameBigrams;
    
    private List<String> acronyms;
    
    private EntityType type;
    
    private EntityQuery(Builder builder) {
        this.rawNames = new ArrayList<>(builder.rawNames);
        this.cleansedNames = new ArrayList<>(builder.cleansedNames);
        this.nameUnigrams = new ArrayList<>(builder.nameUnigrams);
        this.nameBigrams = new ArrayList<>(builder.nameBigrams);
        this.acronyms = new ArrayList<>(builder.acronyms);
        this.type = builder.type;
    }
    
    public List<String> getRawNames() {
        return rawNames;
    }

    public List<String> getCleansedNames() {
        return cleansedNames;
    }
    
    public List<String> getNameUnigrams() {
        return nameUnigrams;
    }

    public List<String> getNameBigrams() {
        return nameBigrams;
    }

    public List<String> getAcronyms() {
        return acronyms;
    }
    
    public EntityType getType() { 
        return type;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Collection<String> rawNames;
        private Collection<String> cleansedNames;
        private Collection<String> nameUnigrams;
        private Collection<String> nameBigrams;
        private Collection<String> acronyms;
        private EntityType type;
        
        private Builder() {
            this.rawNames = new ArrayList<>();
            this.nameUnigrams = new ArrayList<>();
            this.nameBigrams = new ArrayList<>();
            this.acronyms = new ArrayList<>();
        }
        
        public Builder withRawNames(Collection<String> nameVariants) {
            this.rawNames = nameVariants;
            return this;
        }
        
        public Builder withCleansedNames(Collection<String> cleansedNames) {
            this.cleansedNames = cleansedNames;
            return this;
        }
        
        public Builder withNameUnigrams(Collection<String> nameUnigrams) {
            this.nameUnigrams = nameUnigrams;
            return this;
        }
        
        public Builder withNameBigrams(Collection<String> nameBigrams) {
            this.nameBigrams = nameBigrams;
            return this;
        }
        
        public Builder withAcronyms(Collection<String> acronyms) {
            this.acronyms = acronyms;
            return this;
        }
        
        public Builder withType(EntityType type) {
            this.type = type;
            return this;
        }
        
        public EntityQuery build() {
            return new EntityQuery(this);
        }
            
    }
    
}
