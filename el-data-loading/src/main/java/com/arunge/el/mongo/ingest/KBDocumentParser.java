package com.arunge.el.mongo.ingest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.arunge.el.api.InfoboxValue;
import com.arunge.el.api.KBDocument;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class KBDocumentParser {

    private String rootPath = "/knowledge_base";
    
    private String entityPath = rootPath + "/entity";
    
    private static Logger LOG = LoggerFactory.getLogger(KBDocumentParser.class);
    
    XPath xPath = XPathFactory.newInstance().newXPath();

    private XPathExpression entityXpath;
    
    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        String filename = "src/main/resources/test-kb-entry.xml";
        InputStream in = new FileInputStream(new File(filename));
        Document doc = builder.parse(in);
        KBDocumentParser parser = new KBDocumentParser();
        parser.parseDocument(doc);
    }
 
    public KBDocumentParser() {
        try {
            entityXpath = xPath.compile(entityPath);
        } catch (XPathExpressionException e) { 
            
        }
    }
    
    public List<KBDocument> parseDocument(Document doc) throws XPathExpressionException {
        List<KBDocument> documents = new ArrayList<>();
        NodeList entities = evaluate(entityXpath, doc);
        for(int i = 0; i < entities.getLength(); i++) {
            Node entity = entities.item(i);
            entity.getParentNode().removeChild(entity);
            String id = entity.getAttributes().getNamedItem("id").getNodeValue();
            String type = entity.getAttributes().getNamedItem("type").getNodeValue();
            String name = entity.getAttributes().getNamedItem("name").getNodeValue();
//            String title = entity.getAttributes().getNamedItem("wiki_title").getNodeValue();
            Node factsNode = (Node) xPath.evaluate("./facts", entity, XPathConstants.NODE);
            String factsType = factsNode.getAttributes().getNamedItem("class").getNodeValue().replaceAll("[Ii]nfobox\\s*", "");
            Multimap<String, InfoboxValue> facts = extractFacts(factsNode);
//            for(String key : facts.keySet()) {
//                System.out.println(key);
//                for(InfoboxValue val : facts.get(key)) {
//                    System.out.println("\t" + val.getValue() + "," + val.getKbLink());
//                }
//            }
            String wikiText = xPath.evaluate("./wiki_text", entity);
            KBDocument kbDoc = new KBDocument(id);
            kbDoc.setEntityType(type);
            kbDoc.setName(name);
            kbDoc.setDocText(wikiText);
            kbDoc.setInfobox(facts);
            kbDoc.setInfoboxType(factsType);
            documents.add(kbDoc);
        }
        return documents;
    }
    
    private Multimap<String, InfoboxValue> extractFacts(Node factsNode) throws XPathExpressionException {
        Multimap<String, InfoboxValue> mm = HashMultimap.create();
        NodeList factNodes = (NodeList) xPath.evaluate("./fact", factsNode, XPathConstants.NODESET);
        for(int i = 0; i < factNodes.getLength(); i++) {
            Node factNode = factNodes.item(i);
            String factName = factNode.getAttributes().getNamedItem("name").getNodeValue().replaceAll("\\p{Punct}", "");
            String factValue = factNode.getTextContent();
            NodeList childNodes = factNode.getChildNodes();
            InfoboxValue value = new InfoboxValue(factValue);
            for(int j = 0; j < childNodes.getLength(); j++) {
                Node childLink = childNodes.item(j);
                if(childLink.getNodeName().equals("link")) { 
                    NamedNodeMap linkAttrs = childLink.getAttributes();
                    if(linkAttrs != null) {
                        Node entityAttr = linkAttrs.getNamedItem("entity_id");
                        if(entityAttr != null) {
                            value.addKbLink(entityAttr.getNodeValue());
                        }
                    }
                }
            }
            mm.put(factName, value);
        }
        return mm;
    }
    
    private String evaluateSingleValue(XPathExpression expression, Document doc) {
        NodeList l = evaluate(expression, doc);
        if(l == null) {
            return null;
        } else {
            return l.item(0).getTextContent();
        }
    }
    
    private NodeList evaluate(XPathExpression expression, Document doc) {
        try {
            return (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return null;
        }
    }
    
    private NodeList evaluate(String expression, Document doc) {
        try {
            return (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return null;
        }
    }
    
    private NodeList evaluate(String expression, Node node) {
        try {
            return (NodeList) xPath.compile(expression).evaluate(node, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            return null;
        }
    }
    
}
