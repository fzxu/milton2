package com.bradmcevoy.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


public class XmlUtils {

    public static void process(Element n, XmlNodeOperator r) {
        r.process(n);
        for( Object oEl : n.getChildren() ) {
            if( oEl instanceof Element ) {        
                Element child = (Element) oEl;
                process(child, r);
            }
        }
    }

    public static interface XmlNodeOperator {
        void process(Element n);
    }
    
    public static Document getDomDocument(String xml) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        return builder.build(in);
    }
        
}
