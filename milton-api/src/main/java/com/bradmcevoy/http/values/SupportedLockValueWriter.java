package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.http.webdav.WebDavProtocol.SupportedLocks;
import java.util.Map;

public class SupportedLockValueWriter implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return SupportedLocks.class.isAssignableFrom( c );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
		Element supportedLocks = writer.begin("D:supportedlock").open();
        Element lockentry = writer.begin( "D:lockentry" ).open();
        writer.begin( "D:lockscope" ).open(false).writeText( "<D:exclusive/>" ).close();
        writer.begin( "D:locktype" ).open(false).writeText( "<D:write/>" ).close();
        lockentry.close();
		supportedLocks.close();
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
