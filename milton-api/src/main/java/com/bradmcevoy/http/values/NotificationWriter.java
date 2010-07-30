package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import java.util.Map;

/**
  <notification-URL xmlns='http://calendarserver.org/ns/'>
    <href xmlns='DAV:'>/calendars/__uids__/admin/notification/</href>
  </notification-URL>
 * 
 * @author alex
 */
public class NotificationWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return localName.equals( "notification-URL" );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        String urlString = (String) val;
        Element reportSet = writer.begin( "notification-URL" ).open();
        Element report = writer.begin( "href" ).open();
        report.writeText( urlString );
        report.close();
        reportSet.close();
    }

    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
