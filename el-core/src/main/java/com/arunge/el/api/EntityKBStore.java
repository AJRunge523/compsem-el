package com.arunge.el.api;

import java.util.Optional;

import com.arunge.unmei.iterators.CloseableIterator;

public interface EntityKBStore {


    /**
     * Inserts the knowledge base document into the store. If a document with the same ID already exists in the store, this will be a no-op.
     * @param document
     * @return
     */
    String insert(TextEntity document);
    
    /**
     * Retrieves the document with the provided ID from the store if it exists.
     * @param docId
     * @return
     */
    Optional<TextEntity> fetchKBText(String docId);
    
    /**
     * Returns an iterator over all documents in the store.
     * @return
     */
    CloseableIterator<TextEntity> allKBText();
    
    /**
     * Inserts the knowledge base document into the store. If a document with the same ID already exists in the store, this will be a no-op.
     * @param document
     * @return
     */
    String insert(NLPDocument document);
    
    /**
     * Updates the NLPDocument with the specified id with the provided key-value pair.
     * @param field
     * @param value
     */
    void updateNLPDocument(String id, String field, Object value);
    
    /**
     * Clears the specified field from all NLP documents in the store.
     * @param field
     */
    void clearNLPDocument(String field);
    
    /**
     * Retrieves the document with the provided ID from the store if it exists.
     * @param docId
     * @return
     */
    Optional<NLPDocument> fetchNLPDocument(String docId);
    
    /**
     * Returns an iterator over all documents in the store.
     * @return
     */
    CloseableIterator<NLPDocument> allNLPDocuments();
    
    /**
     * Retrieves the entity with the provided ID from the store if it exists.
     * @param entityId
     * @return
     */
    Optional<KBEntity> fetchEntity(String entityId);
        
    /**
     * Inserts the entity into the store. If an entity with the same ID already exists in the store, this will be a no-op.
     * @param entity
     * @return
     */
    String insert(KBEntity entity);
    
    /**
     * Returns an iterator over all entities in the store.
     * @return
     */
    CloseableIterator<KBEntity> allEntities();
   

    
    /**
     * Returns an iterator over the set of entities in the store that match the provided query.
     * @param query
     * @return
     */
    CloseableIterator<KBEntity> query(EntityQuery query);
    
    /**
     * Delete all entities currently in the store. Does not modify the documents.
     */
    void clearEntities();
}
