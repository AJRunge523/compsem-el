package com.arunge.el.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.arunge.el.api.ContextType;
import com.arunge.el.api.EntityKBStore;
import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBEntity;
import com.arunge.el.attribute.DenseVectorAttribute;
import com.arunge.el.attribute.EntityAttribute;
import com.arunge.el.attribute.SparseVectorAttribute;
import com.arunge.el.feature.LevenshteinEditDistance;
import com.arunge.el.nlp.dist.ContextStore;
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

    private EntityKBStore entities;
    private ContextStore contexts;
    
    public EntityCandidateRetrievalEngine(EntityKBStore entities, ContextStore contexts) { 
        this.entities = entities;
        this.contexts = contexts;
    }
    
    public Stream<KBEntity> retrieveCandidates(KBEntity queryEntity) {
        return retrieveCandidates(queryEntity, ContextType.NORM_TFIDF);
    }
    
    public Stream<KBEntity> retrieveCandidates(KBEntity queryEntity, ContextType... types) {
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
        
        if(queryEntity.getNameUnigrams().isPresent()) {
            queryBuilder = queryBuilder.withNameUnigrams(queryEntity.getNameUnigrams().get());
        }
        

        if(queryEntity.getAcronym().isPresent() && !queryEntity.getAcronym().get().trim().isEmpty()) {
            List<String> acronyms = new ArrayList<>();
            acronyms.add(queryEntity.getAcronym().get());
            queryBuilder = queryBuilder.withAcronyms(acronyms);
        }
        queryBuilder = queryBuilder.withType(queryEntity.getType());
        EntityQuery query = queryBuilder.build();
        
        Stream<KBEntity> results = Iterators.toStream(entities.query(query));
        return applySelectiveFilters(query, queryEntity, results).map(e -> {
            for(ContextType type : types) { 
                if(!type.equals(ContextType.NORM_TFIDF)) { 
                    double[] contextVec = contexts.getContext(type, e.getId());
                    e.setAttribute(EntityAttribute.valueOf(type.name()), DenseVectorAttribute.valueOf(contextVec));
                }
            }
            return e;           
        });
    }
    
    private Stream<KBEntity> applySelectiveFilters(EntityQuery query, KBEntity queryEntity, Stream<KBEntity> entities) {
        return entities.filter(e -> {
            String queryAcr = queryEntity.getAcronym().orElse("").trim();
            String candAcr = e.getAcronym().orElse("").trim();
            
            // There are several stars that start with this number - remove them
            if(e.getCleansedName().startsWith("4")) { 
                return false;
            }
            
            //Filter Condition 1: Remove UNK and person type entities if the query is an acronym
            if((e.getType() == EntityType.UNK || e.getType() == EntityType.PERSON) && !(e.getCleansedName().equals(queryEntity.getCleansedName()))) {
                if(!queryAcr.isEmpty()) {
                    
                    if(queryAcr.equals(queryEntity.getName())) {
                        return false;
                    }
                }
            }
            
            // Condition 2: If the candidate has been retrieved via an acronym, then we apply stricter filtering based on edit distance to its aliases
            if(query.getAcronyms().contains(candAcr) && !query.getAcronyms().contains(queryEntity.getName())) {
                
                double currentBest = 1.0;
                boolean matchFound = false;
                if(e.getCleansedAliases().isPresent()) {
                    
                    for(String qcn : query.getCleansedNames()) {
                        for(String ccn : e.getCleansedAliases().get()) { 
                            double distance = LevenshteinEditDistance.computeNormalizedEditDistance(qcn, ccn);
                            if(distance < currentBest) {
                                currentBest = distance;
                            }
                            if(distance < 0.3) {
                                matchFound = true;
                                break;
                            }
                        }
                        if(matchFound) {
                            break;
                        }
                        double distance = LevenshteinEditDistance.computeNormalizedEditDistance(qcn, e.getCleansedName());
                        if(distance < currentBest) {
                            currentBest = distance;
                        }
                        if(distance < 0.3) { 
                            matchFound = true;
                            break;
                        }
                    }
                }
                if(!matchFound) {
                    return false;
                }
            }
            
            //Filter condition 3: Remove single-word location results that don't start with the query term. Based on observations of how locations tend to be referenced
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
