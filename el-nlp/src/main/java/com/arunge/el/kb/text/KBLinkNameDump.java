package com.arunge.el.kb.text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import com.arunge.el.api.TextEntity;
import com.arunge.el.store.mongo.MongoEntityStore;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.mongodb.MongoClient;

public class KBLinkNameDump {

    public static void main(String[] args) throws IOException { 
        String outFile = "output/links.txt";
        MongoClient client = new MongoClient("localhost", 27017);
        MongoEntityStore store = new MongoEntityStore(client, "entity_store");
        Set<String> ids = Sets.newHashSet(Files.readLines(new File("src/main/resources/candIds.txt"), Charsets.UTF_8));
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outFile)))) {
            for(String id : ids) {
                TextEntity te = store.fetchKBText(id).get();
                writer.write(te.getId() + "\t" + te.getSingleMetadata("wiki_title").get() + "\n");
            }
        }
        client.close();
    }
}
