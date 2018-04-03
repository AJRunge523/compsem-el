package com.arunge.el.kb.text;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class KBTextDump {

    public static void main(String[] args) throws IOException {
        String outDir = "J:\\entity-linking-data\\eval-queries";
        MongoEntityStore store = new MongoEntityStore(new MongoClient("localhost", 27017), "el_eval_query_store");
        CloseableIterator<TextEntity> text = store.allKBText();
        while(text.hasNext()) {
            TextEntity te = text.next();
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(outDir, te.getId() + ".txt").toFile()))) {
                writer.write(te.getDocText());
            }
        }
    }
    
}
