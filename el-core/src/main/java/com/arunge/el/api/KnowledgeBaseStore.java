package com.arunge.el.api;

import java.util.Optional;

import com.arunge.unmei.iterators.CloseableIterator;

public interface KnowledgeBaseStore {

    String insert(KBDocument document);
    
    Optional<KBDocument> fetch(String id);
    
    CloseableIterator<KBDocument> all();
    
}
