package com.arunge.el.kb.text;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.nlp.api.AnnotatedToken;
import com.arunge.nlp.api.Tokens;
import com.arunge.nlp.stanford.StanfordNLPPreprocessingPipeline;
import com.arunge.nlp.text.AnnotatedTextDocument;
import com.arunge.nlp.text.AnnotatedTextField;
import com.arunge.nlp.text.TextDocument;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class KBTextDump {

    public static void main(String[] args) throws IOException {
        String outDir = "J:\\entity-linking-data\\eval-queries-sentences";
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = MongoEntityStore.evalStore(client);
        StanfordNLPPreprocessingPipeline pipeline = new StanfordNLPPreprocessingPipeline();
        CloseableIterator<TextEntity> text = store.allKBText();
        while(text.hasNext()) {
            TextEntity te = text.next();
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(outDir, te.getId() + ".txt").toFile()))) {
                AnnotatedTextDocument doc = pipeline.apply(new TextDocument(te.getId(), te.getDocText()));
                for(AnnotatedTextField field : doc.getTextFields().values()) {
                    for(List<AnnotatedToken> tokens : field.getSentences()) {
                        String docSent = Tokens.asString(tokens);
                        docSent.replaceAll("-LRB-", "(");
                        docSent.replaceAll("-RRB-", ")");
                        writer.write(docSent + "\n");
                    }
                }
            }
        }
        client.close();
    }
    
}
