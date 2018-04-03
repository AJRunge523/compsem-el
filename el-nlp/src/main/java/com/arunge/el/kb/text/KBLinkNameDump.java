package com.arunge.el.kb.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.arunge.unmei.iterators.CloseableIterator;
import com.mongodb.MongoClient;

public class KBLinkNameDump {

    public static void main(String[] args) throws IOException { 
        String outFile = "output/links.txt";
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "entity_store");
        CloseableIterator<TextEntity> text = store.allKBText();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFile)))) {
            while(text.hasNext()) {
                TextEntity te = text.next();
//                System.out.println(te.getSingleMetadata("wiki_title"));
                writer.write(te.getId() + "\t" + te.getSingleMetadata("wiki_title").get() + "\n");
            }
        }
        client.close();
    }
}
