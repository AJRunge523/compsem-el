package com.arunge.el.query;

import java.io.File;
import java.nio.file.Paths;

import com.arunge.el.api.ELQuery;

public class QuerySetLoader {

    
    public static Iterable<ELQuery> loadTAC2010Train() {
        String dir = "J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\TAC-KBP KB Train and Test\\TAC_KBP_English_Entity_Linking_2009-2013"
                + "\\TAC_KBP English Train Data\\Train and Eval 09-13\\data\\2010\\training";
        File trainFile = Paths.get(dir, "tac_kbp_2010_english_entity_linking_training_queries.xml").toFile();
        String sourceDir = Paths.get(dir, "source_documents").toString();
        TACParser parser = new TACParser();
        return parser.parse(trainFile, sourceDir);
    }
    
    public static Iterable<ELQuery> loadTAC2010Eval() {
        String dir = "J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\TAC-KBP KB Train and Test\\"
                + "TAC_KBP_English_Entity_Linking_2009-2013\\TAC_KBP English Train Data\\Train and Eval 09-13\\data\\2010\\eval";
        File trainFile = Paths.get(dir, "tac_kbp_2010_english_entity_linking_evaluation_queries.xml").toFile();
        String sourceDir = Paths.get(dir, "source_documents").toString();
        TACParser parser = new TACParser();
        return parser.parse(trainFile, sourceDir);
    }
}
