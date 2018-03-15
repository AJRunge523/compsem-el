package com.arunge.el.kb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.arunge.el.api.EntityMetadataKeys;
import com.arunge.el.api.TextEntity;

public class KBDocumentParser extends DefaultHandler {

    private List<TextEntity> loadedEntities;
    private SAXParser parser;
    private TextEntity currentEntity;
    private String currentFact;
    private String currentFactVal;
    private String currentLinkVal;
    private String currentLink;
    private String currentText;
    
    private boolean sFact;
    private boolean sText;
    private boolean sLink;
    
    
    private static Logger LOG = LoggerFactory.getLogger(KBDocumentParser.class);
    
    public static void main(String[] args) throws Exception {
        String filename = "src/main/resources/test-kb-entry.xml";
        File kbFile = new File(filename);
        KBDocumentParser parser = new KBDocumentParser();
        List<TextEntity> textEntities = parser.parseDocument(kbFile);
        for(TextEntity e : textEntities) {
            
            System.out.println(e.getId() + ", " + e.getName() + ", " + e.getEntityType());
        }
    }
 
    public KBDocumentParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            this.parser = factory.newSAXParser();
        } catch (Exception e) { 
            throw new RuntimeException("Failed to initialize the XML parser.", e);
        }
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals("entity")) {
            String name = attributes.getValue("name");
            String type = attributes.getValue("type");
            String title = attributes.getValue("wiki_title");
            String id = attributes.getValue("id");
            currentEntity = new TextEntity(id);
            currentEntity.setName(name);
            currentEntity.setEntityType(type);
            currentEntity.putMetadata(EntityMetadataKeys.TITLE, title);
        } else if(qName.equals("facts")) {
            String ibType = attributes.getValue("class").replaceAll("[Ii]nfobox\\s*", "").toLowerCase();
            currentEntity.putMetadata(EntityMetadataKeys.INFOBOX_TYPE, ibType);
        } else if(qName.equals("fact")) { 
            currentFact = attributes.getValue("name").replaceAll("\\p{Punct}", "");
            currentFactVal = "";
            sFact = true;
        } else if(qName.equals("link")) {
            String eId = attributes.getValue("entity_id");
            currentLinkVal = "";
            currentLink = eId;
            sLink = true;
        } else if(qName.equals("wiki_text")) { 
            sText = true;
            currentText = "";
        }
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        if(sLink) {
            currentLinkVal += new String(ch, start, length);
        } 
        if(sFact) { 
            currentFactVal += new String(ch, start, length);
        } else if(sText) {
            currentText += new String(ch, start, length);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("entity")) {
            loadedEntities.add(currentEntity);
            currentEntity = null;
        } else if(qName.equals("fact")) {
            if(currentFactVal != null && !currentFactVal.trim().isEmpty() && !currentFactVal.equals(currentLinkVal)) {
                currentEntity.putMetadata(EntityMetadataKeys.INFOBOX_KEY_PREFIX + currentFact, currentFactVal);
            }
            currentFact = "";
            sFact = false;
        } else if(qName.equals("link")) {
            String value = currentLinkVal + EntityMetadataKeys.INFOBOX_SEPARATOR;
            if(currentLink != null) {
                value = value + currentLink;
            }
            currentEntity.putMetadata(EntityMetadataKeys.INFOBOX_KEY_PREFIX + currentFact, value);
            sLink = false;
        } else if(qName.equals("wiki_text")) {
            currentEntity.setDocText(currentText);
            sText = false;
        } 
    }
    
    public List<TextEntity> parseDocument(File kbFile) {
        loadedEntities = new ArrayList<>();
        try {
            parser.parse(kbFile, this);
        } catch (SAXException | IOException e) {
            throw new RuntimeException("Failed to parse kb file: " + kbFile.getAbsolutePath(), e);
        }
        return loadedEntities;
    }
    
}
