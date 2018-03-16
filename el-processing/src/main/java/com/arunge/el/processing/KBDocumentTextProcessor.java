package com.arunge.el.processing;

import com.arunge.el.api.NLPDocument;
import com.arunge.el.api.TextEntity;

public class KBDocumentTextProcessor {

//    private NLPPreprocessingPipeline pipeline;
    
//    Annotator[] FULL_ANNOTATORS = {Annotator.POS, Annotator.LEMMA, Annotator.DEPPARSE, Annotator.NER/*Annotator.DEPPARSE, Annotator.POS, Annotator.NER, Annotator.LEMMA*/};
    
    public KBDocumentTextProcessor() {
//        this.pipeline = new StanfordNLPPreprocessingPipeline(FULL_ANNOTATORS);
    }

    public NLPDocument process(TextEntity text) {
        NLPDocument output = new NLPDocument(text.getId());
//        String text = Arrays.stream(document.getDocText().split("\n\n"))
//                .filter(t -> t.split(" ").length >= 5).reduce((a, b) -> a + "\n" + b)
//                .get();
//        
        
//        TextDocument textDoc = new TextDocument(document.getId(), text);
//        PreprocessedTextDocument processed = pipeline.apply(textDoc);
//        for(PreprocessedTextField field : processed.getTextFields().values()) {
//            System.out.println(field.render(FULL_ANNOTATORS));
//        }
        return output;
    }
    
}
