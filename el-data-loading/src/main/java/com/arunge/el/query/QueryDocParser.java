package com.arunge.el.query;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class QueryDocParser {

    private DocumentBuilder builder;
    
    XPath xPath = XPathFactory.newInstance().newXPath();
    
    XPathExpression textPath;
    
    public static void main(String[] args) throws Exception { 
        QueryDocParser parser = new QueryDocParser();
        List<String> test = Files.readLines(new File("J:\\Education\\CMU\\2018\\Spring\\Computational Semantics\\Entity Linking\\TAC-KBP KB Train and Test\\TAC_KBP_English_Entity_Linking_2009-2013\\"
                + "TAC_KBP English Train Data\\Train and Eval 09-13\\data\\2010\\training\\source_documents\\eng-NG-31-100025-10766602.xml"), Charsets.UTF_8);
        
        String xmlText = test.stream().reduce("", (a, b) -> a + "\n" + b);
        
        String parsedText = parser.getDocText(xmlText);
        System.out.println(parsedText);
    }
    
    public QueryDocParser() throws ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        builder = factory.newDocumentBuilder();
        textPath = xPath.compile("DOC/BODY/TEXT/POST"); 
    }
 
    public String getDocText(String xmlText) throws SAXException, IOException, XPathExpressionException { 
        ByteArrayInputStream input = new ByteArrayInputStream(xmlText.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        NodeList nl = (NodeList) textPath.evaluate(doc, XPathConstants.NODESET);
        return nl.item(0).getTextContent();
    }
    
}
