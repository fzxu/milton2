package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.Utils;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.webdav.WebDavProtocol.SupportedLocks;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PropFindXmlGenerator {

    private static final Logger log = LoggerFactory.getLogger( PropFindXmlGenerator.class );
    private final Helper helper;
    private final ValueWriter valueWriter;

    public PropFindXmlGenerator() {
        helper = new Helper();
        List<ValueWriter> writers = new ArrayList<ValueWriter>();
        writers.add( new LockTokenValueWriter() );
        writers.add( new SupportedLockValueWriter() );
        writers.add( new ModifiedDateValueWriter() );
        writers.add( new DateValueWriter() );
        writers.add( new ResourceTypeValueWriter() );
        writers.add( new BooleanValueWriter() );
        writers.add( new ToStringValueWriter() );
        valueWriter = new MultiValueWriter( writers );
    }

    public PropFindXmlGenerator( List<ValueWriter> writers ) {
        helper = new Helper();
        valueWriter = new MultiValueWriter( writers );
    }

    PropFindXmlGenerator( Helper helper, ValueWriter valueWriter ) {
        this.helper = helper;
        this.valueWriter = valueWriter;
    }

    public void generate( List<PropFindResponse> propFindResponses, OutputStream responseOutput ) {
        Map<String, String> mapOfNamespaces = helper.findNameSpaces( propFindResponses );
        ByteArrayOutputStream generatedXml = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter( generatedXml );
        writer.writeXMLHeader();
        writer.open( "D:multistatus" + helper.generateNamespaceDeclarations( mapOfNamespaces ) );
        writer.newLine();
        helper.appendResponses( writer, propFindResponses, mapOfNamespaces );
        writer.close( "D:multistatus" );
        writer.flush();
        log.debug( generatedXml.toString() );
        helper.write( generatedXml, responseOutput );

    }

    class Helper {

        /**
         *
         * @param propFindResponses
         * @return - map where key is the uri, and value is the prefix
         */
        Map<String, String> findNameSpaces( List<PropFindResponse> propFindResponses ) {
            int i = 1;
            Map<String, String> map = new HashMap<String, String>();
            for( PropFindResponse r : propFindResponses ) {
                for( QName p : r.getKnownProperties().keySet() ) {
                    String uri = p.getNamespaceURI();
//                    if( uri.endsWith( ":" ) ) uri = uri.substring( 0, uri.length() - 1 ); // strip trailing :
                    if( !map.containsKey( uri ) ) {
                        if( uri.equals( WebDavProtocol.NS_DAV ) ) {
                            map.put( uri, "D" );
                        } else {
                            map.put( uri, "ns" + i++ );
                        }
                    }
                }
            }
            return map;
        }

        String generateNamespaceDeclarations( Map<String, String> mapOfNamespaces ) {
            String decs = "";
            for( String uri : mapOfNamespaces.keySet() ) {
                String prefix = mapOfNamespaces.get( uri );
                decs += " xmlns:" + prefix + "=\"" + uri + "\"";
            }
            return decs;
        }

        void appendResponses( XmlWriter writer, List<PropFindResponse> propFindResponses, Map<String, String> mapOfNamespaces ) {
            for( PropFindResponse r : propFindResponses ) {
                XmlWriter.Element el = writer.begin( "D:response" );
                el.open();
                writer.writeProperty( "D", "href", r.getHref() );
                sendKnownProperties( writer, mapOfNamespaces, r.getKnownProperties(), r.getHref() );
                sendUnknownProperties( writer, mapOfNamespaces, r.getUnknownProperties() );
                el.close();
            }
        }

        private void sendKnownProperties( XmlWriter writer, Map<String, String> mapOfNamespaces, LinkedHashMap<QName, Object> properties, String href ) {
            if( !properties.isEmpty() ) {
                XmlWriter.Element elPropStat = writer.begin( "D:propstat" ).open();
                XmlWriter.Element elProp = writer.begin( "D:prop" ).open();
                for( QName qname : properties.keySet() ) {
                    String prefix = mapOfNamespaces.get( qname.getNamespaceURI() );
                    Object val = properties.get( qname );
                    valueWriter.writeValue( writer, qname.getNamespaceURI(), prefix, qname.getLocalPart(), val, href, mapOfNamespaces );
                }
                elProp.close();
                writer.writeProperty( "D", "status", "HTTP/1.1 200 Ok" );
                elPropStat.close();
            }
        }

        private void sendUnknownProperties( XmlWriter writer, Map<String, String> mapOfNamespaces, List<QName> properties ) {
            if( !properties.isEmpty() ) {
                XmlWriter.Element elPropStat = writer.begin( "D:propstat" ).open();
                XmlWriter.Element elProp = writer.begin( "D:prop" ).open();
                for( QName qname : properties ) {
                    String prefix = mapOfNamespaces.get( qname.getNamespaceURI() );
                    writer.writeProperty( prefix, qname.getLocalPart() );
                }
                elProp.close();
                writer.writeProperty( "D", "status", "HTTP/1.1 404 Not Found" );
                elPropStat.close();
            }
        }

        void write( ByteArrayOutputStream out, OutputStream outputStream ) {
            try {
                String xml = out.toString( "UTF-8" );
                outputStream.write( xml.getBytes() ); // note: this can and should write to the outputstream directory. but if it aint broke, dont fix it...
            } catch( UnsupportedEncodingException ex ) {
                throw new RuntimeException( ex );
            } catch( IOException ex ) {
                throw new RuntimeException( ex );
            }
        }
    }

    class MultiValueWriter implements ValueWriter {

        private final List<ValueWriter> writers;

        public MultiValueWriter( List<ValueWriter> writers ) {
            this.writers = writers;
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            for( ValueWriter vw : writers ) {
                if( vw.supports( prefix, nsUri, localName, val ) ) {
                    vw.writeValue( writer, nsUri, prefix, localName, val, href, nsPrefixes );
                    break;
                }
            }
        }

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return true;
        }
    }

    public static interface ValueWriter {

        boolean supports( String prefix, String nsUri, String localName, Object val );

        void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes );
    }

    public class LockTokenValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return val instanceof LockToken;
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            LockToken token = (LockToken) val;
            Element lockentry = writer.begin( "D:lockdiscovery" ).open();
            if( token != null ) {
                LockInfo info = token.info;
                writer.begin( "D:lockscope" ).open().writeText( "<D:" + info.scope.name().toLowerCase() + "/>" ).close();
                writer.begin( "D:locktype" ).open().writeText( "<D:" + info.type.name().toLowerCase() + "/>" ).close();
                writer.begin( "D:depth" ).open().writeText( "0" ).close();
                writer.begin( "D:owner" ).open().writeText( info.owner ).close();
                writer.begin( "D:timeout" ).open().writeText( token.timeout.toString() ).close();

                Element elToken = writer.begin( "D:locktoken" ).open();
                writer.begin( "D:href" ).open().writeText( "urn:uuid:" + token.tokenId ).close();
                writer.begin( "D:lockroot" ).open().writeText( href ).close();
                elToken.close();
            }
            lockentry.close();

        }
    }

    public class SupportedLockValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return val instanceof SupportedLocks;
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            Element lockentry = writer.begin( "lockentry" ).open();
            writer.begin( "lockscope" ).open().writeText( "<D:exclusive/>" ).close();
            writer.begin( "locktype" ).open().writeText( "<D:write/>" ).close();
            lockentry.close();
        }
    }

    public class ResourceTypeValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return localName.equals( "resourcetype" );
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            log.debug( "write: resourcetype: " + val);
            List<QName> list = (List<QName>) val;
            if( list != null && list.size() > 0 ) {
                Element rt = writer.begin( prefix, localName );
                for( QName name : list ) {
                    String childNsUri = name.getNamespaceURI();
                    String childPrefix = nsPrefixes.get( childNsUri);
                    rt.begin(childPrefix, name.getLocalPart()).noContent(); 
                }
                rt.close();
            } else {
                writer.writeProperty( prefix, localName );
            }
        }
    }

    public class ModifiedDateValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return ( nsUri.equals( WebDavProtocol.NS_DAV ) && localName.equals( "getlastmodified" ) );
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
            Date dt = (Date) val;
            String f;
            if( dt == null ) {
                f = "";
            } else {
                f = DateUtils.formatForWebDavModifiedDate( dt );
            }
            writer.writeProperty( prefix, localName, f );
        }
    }

    public class DateValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return val instanceof Date;
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            Date date = (Date) val;
            String s = DateUtils.formatDate( date );
            writer.writeProperty( prefix, localName, s );
        }
    }

    public class BooleanValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return val instanceof Boolean;
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            Boolean b = (Boolean) val;
            writer.writeProperty( prefix, localName, b.toString().toUpperCase() ); // i think that ms wants uppercase
        }
    }

    public class ToStringValueWriter implements ValueWriter {

        public boolean supports( String prefix, String nsUri, String localName, Object val ) {
            return val != null;
        }

        private String nameEncode( String s ) {
            //return Utils.encode(href, false); // see MIL-31
            return Utils.escapeXml( s );
            //return href.replaceAll("&", "&amp;");  // http://www.ettrema.com:8080/browse/MIL-24
        }

        public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
            String s = nameEncode( val.toString() );
            writer.writeProperty( prefix, localName, s );
        }
    }
}
