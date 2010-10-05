package com.bradmcevoy.http.values;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import java.util.Date;
import java.util.Map;

/**
 * BM: not sure if we want or need this. Must be here for a reason, but i
 * cant see anywhere that the modified date should be in a different format
 *  to the creation date
 *
 * @author brad
 */
public class ModifiedDateValueWriter implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return nsUri.equals( WebDavProtocol.NS_DAV.getName() ) && localName.equals( "getlastmodified" );
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

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
