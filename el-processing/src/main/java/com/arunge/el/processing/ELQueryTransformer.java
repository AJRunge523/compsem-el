package com.arunge.el.processing;

import com.arunge.el.api.ELQuery;
import com.arunge.el.api.KBEntity;

public interface ELQueryTransformer {

    /**
     * Transforms the provided {@link KBEntity} with additional information 
     * based on the original {@link ELQuery}. Implementations should make
     * all necessary modifications directly to the KBEntity at this stage.
     * @param entity
     * @param query
     */
    void transform(KBEntity entity, ELQuery query);
    
}
