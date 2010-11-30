package com.ettrema.httpclient;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ReportMethod extends EntityEnclosingMethod {

    private static final Logger log = LoggerFactory.getLogger( PropFindMethod.class );

    public ReportMethod( String uri ) {
        super( uri );
    }

    @Override
    public String getName() {
        return "REPORT";
    }

    public Document getResponseAsDocument() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = getResponseBodyAsStream();
        IOUtils.copy( in, out );
        String xml = out.toString();
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read( new ByteArrayInputStream( xml.getBytes() ) );
            return document;
        } catch( DocumentException ex ) {
            throw new RuntimeException(xml, ex );
        }
    }
}
