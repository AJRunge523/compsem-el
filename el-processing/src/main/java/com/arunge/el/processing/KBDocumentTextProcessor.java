package com.arunge.el.processing;

import java.util.Arrays;

import com.arunge.el.api.EntityType;
import com.arunge.el.api.KBDocument;
import com.arunge.nlp.api.Annotator;
import com.arunge.nlp.api.NLPPreprocessingPipeline;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.PreprocessedTextDocument;
import com.arunge.nlp.text.PreprocessedTextField;
import com.arunge.nlp.text.TextDocument;

public class KBDocumentTextProcessor {

    private NLPPreprocessingPipeline pipeline;
    
    Annotator[] FULL_ANNOTATORS = {Annotator.POS, Annotator.LEMMA, Annotator.DEPPARSE, Annotator.NER/*Annotator.DEPPARSE, Annotator.POS, Annotator.NER, Annotator.LEMMA*/};
    
    public KBDocumentTextProcessor() {
        this.pipeline = new StanfordNLPPreprocessingPipeline(FULL_ANNOTATORS);
    }

    public NLPKBDocument process(KBDocument document) {
        NLPKBDocument output = new NLPKBDocument(document.getId());
        output.setType(parseType(document.getEntityType()));
        String text = Arrays.stream(document.getDocText().split("\n\n"))
                .filter(t -> t.split(" ").length >= 5).reduce((a, b) -> a + "\n" + b)
                .get();
        
        
        TextDocument textDoc = new TextDocument(document.getId(), text);
        PreprocessedTextDocument processed = pipeline.apply(textDoc);
        for(PreprocessedTextField field : processed.getTextFields().values()) {
            System.out.println(field.render(FULL_ANNOTATORS));
        }
        return output;
    }
    
    private EntityType parseType(String eType) {
        if(eType.toLowerCase().equals("per")) {
            return EntityType.PERSON;
        } else if(eType.toLowerCase().equals("gpe")) {
            return EntityType.GPE;
        } else if(eType.toLowerCase().equals("org")) {
            return EntityType.ORG;
        } else {
            return EntityType.UNK;
        }
    }
    
    
}
