package com.arunge.el.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.arunge.el.api.EntityQuery;
import com.arunge.el.api.EntityKBStore;
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
        names.add(queryEntity.getName());
        names.add(queryEntity.getCleansedName());
        Optional<Set<String>> aliases = queryEntity.getAliases();
        if(aliases.isPresent()) {
            names.addAll(aliases.get());
        }
        queryBuilder = queryBuilder.withNameVariants(names);
        if(queryEntity.getNameUnigrams().isPresent()) {
            queryBuilder = queryBuilder.withNameUnigrams(queryEntity.getNameUnigrams().get());
        }
        EntityQuery query = queryBuilder.build();
        
        return Iterators.toStream(store.query(query));
    }
    
}
