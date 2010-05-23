package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import java.util.Map;

/**
 *
 * @author alex
 */
public class SupportedReportSetWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return SupportedReportSetList.class.isAssignableFrom( c );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        
      
        SupportedReportSetList list = (SupportedReportSetList) val;
        Element reportSet = writer.begin( "supported-report-set" ).open();
        if( list != null ) {
            for( String s : list) {
                Element report = writer.begin( "supported-report" ).open();
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
