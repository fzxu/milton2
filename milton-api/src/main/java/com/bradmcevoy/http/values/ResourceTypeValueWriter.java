package com.bradmcevoy.http.values;

import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.XmlWriter.Element;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

public class ResourceTypeValueWriter implements ValueWriter {

    public boolean supports( String nsUri, String localName, Class c ) {
        return localName.equals( "resourcetype" );
    }

    public void writeValue( XmlWriter writer, String nsUri, String prefix, String localName, Object val, String href, Map<String, String> nsPrefixes ) {
        List<QName> list = (List<QName>) val;
        if( list != null && list.size() > 0 ) {
            Element rt = writer.begin( prefix, localName );
            for( QName name : list ) {
                String childNsUri = name.getNamespaceURI();
                String childPrefix = nsPrefixes.get( childNsUri );
                rt.begin( childPrefix, name.getLocalPart() ).noContent();
            }
            rt.close();
        } else {
            writer.writeProperty( prefix, localName );
        }
    }


    public Object parse( String namespaceURI, String localPart, String value ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
