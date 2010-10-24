package com.ettrema.httpclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class PropFindMethod extends EntityEnclosingMethod {

    private static final Logger log = LoggerFactory.getLogger( PropFindMethod.class );

    public PropFindMethod( String uri ) {
        super( uri );
    }

    @Override
    public String getName() {
        return "PROPFIND";
    }

    public Document getResponseAsDocument() throws IOException {
        MyByteArrayOutputStream out = new MyByteArrayOutputStream();
        out.readFully( getResponseBodyAsStream() );
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read( out.asIn() );
            return document;
        } catch( DocumentException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public List<Response> getResponses() {
        List<Response> responses = new ArrayList<PropFindMethod.Response>();
        try {
            Header serverDateHeader = this.getResponseHeader( "Date");
            String serverDate = null;
            if( serverDateHeader != null ) {
                serverDate = serverDateHeader.getValue();
            }
            Document document = getResponseAsDocument();
            if( document == null ) {
                return responses;
            }
            Element root = document.getRootElement();
            Iterator it = root.elementIterator( "response" );
            while( it.hasNext() ) {
                Element el = (Element) it.next();
                Response resp = new Response( serverDate, el );
                responses.add( resp );
            }
            return responses;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public static class Response {

        final String name;
        final String parentHref;
        final String displayName;
        final String href;
        final String modifiedDate;
        final String createdDate;
        final String serverDate;
        final String contentType;
        final Long contentLength;
        final Long quotaAvailableBytes;
        final Long quotaUsedBytes;
        final Long crc;
        final boolean isCollection;

        public Response( String serverDate, Element elResponse ) {
            this.serverDate = serverDate;
            href = RespUtils.asString( elResponse, "href" ).trim();
            int pos = href.lastIndexOf( "/", 8 );
            if( pos > 0 ) {
                parentHref = href.substring( 0, pos - 1 );
            } else {
                parentHref = null;
            }

            Element el = elResponse.element( "propstat" ).element( "prop" );
            String[] arr = href.split( "[/]" );
            name = arr[arr.length - 1];
            String dn = RespUtils.asString( el, "displayname" );
            displayName = ( dn == null ) ? name : dn;
            createdDate = RespUtils.asString( el, "creationdate" );
            modifiedDate = RespUtils.asString( el, "getlastmodified" );
            contentType = RespUtils.asString( el, "getcontenttype" );
            contentLength = RespUtils.asLong( el, "getcontentlength" );
            quotaAvailableBytes = RespUtils.asLong( el, "quota-available-bytes" );
            quotaUsedBytes = RespUtils.asLong( el, "quota-used-bytes" );
            crc = RespUtils.asLong( el, "crc" );
            isCollection = RespUtils.hasChild( el.element( "resourcetype" ), "collection" );
        }
    }
}
