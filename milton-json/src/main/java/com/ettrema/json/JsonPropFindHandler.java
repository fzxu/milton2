package com.ettrema.json;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.webdav.PropFindPropertyBuilder;
import com.bradmcevoy.http.webdav.PropFindRequestFieldParser.ParseResult;
import com.bradmcevoy.http.webdav.PropFindResponse;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class JsonPropFindHandler {

    private static final Logger log = LoggerFactory.getLogger( JsonPropFindHandler.class );

    private final PropFindPropertyBuilder propertyBuilder;

    private final Helper helper;

    public JsonPropFindHandler( PropFindPropertyBuilder propertyBuilder ) {
        this.propertyBuilder = propertyBuilder;
        helper = new Helper();
    }

    public JsonPropFindHandler() {
        this.propertyBuilder = null;
        helper = new Helper();
    }





    public void sendContent( PropFindableResource wrappedResource, String encodedUrl, OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        JsonConfig cfg = new JsonConfig();
        cfg.setCycleDetectionStrategy( CycleDetectionStrategy.LENIENT );

        JSON json;
        Writer writer = new PrintWriter( out );
        String[] arr;
        if( propertyBuilder == null ) {
            if( wrappedResource instanceof CollectionResource ) {
                List<? extends Resource> children = ( (CollectionResource) wrappedResource ).getChildren();
                json = JSONSerializer.toJSON( toSimpleList( children ), cfg );
            } else {
                json = JSONSerializer.toJSON( toSimple( wrappedResource ), cfg );
            }
        } else {
            // use propfind handler
            String sFields = params.get( "fields" );
            Set<QName> fields = new HashSet<QName>();
            if( sFields != null && sFields.length() > 0 ) {
                arr = sFields.split( "," );
                for( String s : arr ) {
                    QName qn;
                    if( s.contains( ":")) {
                        // name is of form uri:local  Eg MyDav:authorName
                        String parts[] = s.split( ":");
                        String nsUri = parts[0];
                        String localName = parts[1];
                        qn = new QName( nsUri, localName);
                    } else {
                        // name is simple form Eg displayname, default nsUri to DAV
                        qn = new QName( WebDavProtocol.NS_DAV, s);
                    }
                    fields.add( qn );
                }
            }

            String sDepth = params.get( "depth" );
            int depth = 1;
            if( sDepth != null && sDepth.trim().length() > 0 ) {
                depth = Integer.parseInt( sDepth );
            }

            String href = encodedUrl.replace( "/_DAV/PROPFIND", "");
            log.debug( "href: " + href);
            ParseResult parseResult = new ParseResult( false, fields);
            List<PropFindResponse> props = propertyBuilder.buildProperties( wrappedResource, depth, parseResult, href );
            List<Map<String, Object>> list = helper.toMap( props );
            json = JSONSerializer.toJSON( list, cfg );
        }
        json.write( writer );
        writer.flush();
    }

    private List<SimpleResource> toSimpleList( List<? extends Resource> list ) {
        List<SimpleResource> simpleList = new ArrayList<SimpleResource>( list.size() );
        for( Resource r : list ) {
            simpleList.add( toSimple( r ) );
        }
        return simpleList;
    }

    private SimpleResource toSimple( Resource r ) {
        return new SimpleResource( r );
    }

    public class SimpleResource {

        private final Resource r;

        public SimpleResource( Resource r ) {
            this.r = r;
        }

        public String getName() {
            return r.getName();
        }

        public Date getModifiedDate() {
            return r.getModifiedDate();
        }
    }

    class Helper {

        private List<Map<String, Object>> toMap( List<PropFindResponse> props ) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            for( PropFindResponse prop : props) {
                Map<String, Object> map = new HashMap<String, Object>();
                list.add( map );
                for( Entry<QName, Object> p : prop.getKnownProperties().entrySet()) {
                    map.put( p.getKey().getLocalPart(), p.getValue());
                }
            }
            return list;
        }

    }
}
