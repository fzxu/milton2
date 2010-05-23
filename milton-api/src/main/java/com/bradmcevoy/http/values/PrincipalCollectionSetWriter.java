package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import java.util.Map;

/**
 *
 * @author alex
 */
public class PrincipalCollectionSetWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return SupportedReportSetList.class.isAssignableFrom( c );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        
      
        PrincipalCollectionSetList list = (PrincipalCollectionSetList) val;
        Element reportSet = writer.begin( "principal-collection-set" ).open();
        if( list != null ) {
            for( String s : list) {
                Element report = writer.begin( "href" ).open();
                report.writeText( s );
                report.close();
            }
        }
        reportSet.close();
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
