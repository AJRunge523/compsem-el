package com.arunge.el.query;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.arunge.el.api.ELQuery;

public class TACParser extends DefaultHandler {

    private List<ELQuery> currentQueries;
    private SAXParser parser;
    private ELQuery currentQuery;
    private String currentVal;
    private String sourceDir;
    
    private boolean sName = false;
    private boolean sId = false;
    private boolean sEntity = false;
    
    public TACParser() { 
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
        if(qName.equals("query")) {
            currentQuery = new ELQuery();
            String id = attributes.getValue("id");
            currentQuery.setQueryId(id);
        } else if(qName.equals("name")) {
            sName = true;
        } else if(qName.equals("docid")) {
            sId = true;
        } else if(qName.equals("entity")) {
            sEntity = true;
        }
    }
    
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        currentVal += new String(ch, start, length);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentVal = currentVal.trim();
        if(qName.equals("query")) {
            currentQueries.add(currentQuery);
            currentQuery = null;
        } else if(qName.equals("name")) {
            currentQuery.setName(currentVal);
            sName = false;
        } else if(qName.equals("docid")) {
            currentQuery.setDocPath(Paths.get(sourceDir, currentVal).toString());
            sId = false;
        } else if(qName.equals("entity")) {
            currentQuery.setGoldEntity(currentVal);
            sEntity = false;
        }
        currentVal = "";
    }
    
    public List<ELQuery> parse(File queryFile, String sourceDir) {
        currentQueries = new ArrayList<>();
        this.sourceDir = sourceDir;
        try {
            parser.parse(queryFile, this);
        } catch (SAXException | IOException e) {
            throw new RuntimeException("Failed to parse query file: ." + queryFile.getAbsolutePath(), e);
        }
        return currentQueries;
    }
    
}
