package com.arunge.el.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityQuery {

    private List<String> nameVariants;
    
    private List<String> nameUnigrams;
    
    private List<String> nameBigrams;
    
    private List<String> acronyms;
    
    private EntityQuery(Builder builder) {
        this.nameVariants = new ArrayList<>(builder.nameVariants);
        this.nameUnigrams = new ArrayList<>(builder.nameUnigrams);
        this.nameBigrams = new ArrayList<>(builder.nameBigrams);
        this.acronyms = new ArrayList<>(builder.acronyms);
    }
    
    public List<String> getNameVariants() {
        return nameVariants;
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
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Collection<String> nameVariants;
        private Collection<String> nameUnigrams;
        private Collection<String> nameBigrams;
        private Collection<String> acronyms;
        
        private Builder() {
            this.nameVariants = new ArrayList<>();
            this.nameUnigrams = new ArrayList<>();
            this.nameBigrams = new ArrayList<>();
            this.acronyms = new ArrayList<>();
        }
        
        public Builder withNameVariants(Collection<String> nameVariants) {
            this.nameVariants = nameVariants;
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
        
        public EntityQuery build() {
            return new EntityQuery(this);
        }
            
    }
    
}
