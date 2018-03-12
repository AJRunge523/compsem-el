package com.arunge.el.api;

import java.util.Optional;

import com.arunge.unmei.iterators.CloseableIterator;

public interface EntityStore {

    /**
     * Inserts the entity into the store. If an entity with the same ID already exists in the store, this will be a no-op.
     * @param entity
     * @return
     */
    String insert(KBEntity entity);
    
    /**
     * Retrieves the entity with the provided ID from the store, if it exists.
     * @param id
     * @return
     */
    Optional<KBEntity> fetch(String id);
    
    /**
     * Returns an iterator over all entities in the store.
     * @return
     */
    CloseableIterator<KBEntity> all();
   
    /**
     * Returns an iterator over hte set of entities in the store that match the provided query.
     * @param query
     * @return
     */
    CloseableIterator<KBEntity> query(EntityQuery query);
}
