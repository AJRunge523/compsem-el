package com.arunge.el.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.unmei.iterators.Iterators;

/**
 * 
 *<p>Engine for retrieving candidate {@link KBEntity} objects for a given query.</p>
 * Candidate retrieval is handled separately in order to more easily facilitate
 * enabling and disabling the features used throughout the pipeline, both at retrieval
 * and at candidate evaluation.
 *
 * @author Andrew Runge
 *
 */
public class EntityCandidateRetrievalEngine {

    private EntityKBStore store;
    
    
    public EntityCandidateRetrievalEngine(EntityKBStore store) { 
        this.store = store;
    }
    
    public Stream<KBEntity> retrieveCandidates(KBEntity queryEntity) {
        EntityQuery.Builder queryBuilder = EntityQuery.builder();
        List<String> names = new ArrayList<>();
        List<String> cleansedNames = new ArrayList<>();
        names.add(queryEntity.getName());
        cleansedNames.add(queryEntity.getCleansedName());
        Optional<Set<String>> aliases = queryEntity.getAliases();
        if(aliases.isPresent()) {
            names.addAll(aliases.get());
        }
        if(queryEntity.getCleansedAliases().isPresent()) {
            cleansedNames.addAll(queryEntity.getCleansedAliases().get());
        }
        queryBuilder = queryBuilder.withRawNames(names)
                .withCleansedNames(cleansedNames);
        if(queryEntity.getAcronym().isPresent() && !queryEntity.getAcronym().get().trim().isEmpty()) {
            List<String> acronyms = new ArrayList<>();
            acronyms.add(queryEntity.getAcronym().get());
            queryBuilder = queryBuilder.withAcronyms(acronyms);
        }
        queryBuilder = queryBuilder.withType(queryEntity.getType());
        EntityQuery query = queryBuilder.build();
        
        return Iterators.toStream(store.query(query)).filter(e -> {
            String queryAcr = queryEntity.getAcronym().orElse("").trim();
            String entAcr = e.getAcronym().orElse("");
            
            //Filter Condition 1: Remove UNK and person type entities if the query is an acronym
            if((e.getType() == EntityType.UNK || e.getType() == EntityType.PERSON) && !(e.getCleansedName().equals(queryEntity.getCleansedName()))) {
                if(!queryAcr.isEmpty()) {
                    if(queryAcr.equals(queryEntity.getName())) {
                        return false;
                    }
                }
            }
            
            //Filter condition 2: Remove single-word location results that don't start with the query term. Based on observations of how locations tend to be referenced
            if(queryEntity.getType() == EntityType.GPE) {
                if(queryEntity.getName().contains(" ") || e.getName().startsWith(queryEntity.getName()) || e.getCleansedName().startsWith(queryEntity.getCleansedName())
                        || e.getAliases().get().contains(queryEntity.getName()) || e.getCleansedAliases().get().contains(queryEntity.getCleansedName())
                        || (e.getAcronym().isPresent() && e.getAcronym().get().equals(queryEntity.getName()))) {
                    return true;
                } 
                return false;
            }
            return true;
            
        });
    }
    
}
