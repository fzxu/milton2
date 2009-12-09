package com.bradmcevoy.http.webdav;

import java.io.InputStream;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Decorator for PropFindRequestFieldParser's.
 *
 * Calls getRequestedFields on the wrapped object. If no fields were requested
 * this class adds the default ones expected
 * by windows clients. This is because windows clients generally do not
 * specify a PROPFIND body and expect the server to return these fields.
 *
 * Note that failing to return exactly the fields expected in the exact order
 * can break webdav on windows.
 *
 * @author brad
 */
public class MsPropFindRequestFieldParser implements PropFindRequestFieldParser{

    private final PropFindRequestFieldParser wrapped;

    public MsPropFindRequestFieldParser( PropFindRequestFieldParser wrapped ) {
        this.wrapped = wrapped;
    }

    public MsPropFindRequestFieldParser() {
        wrapped = new DefaultPropFindRequestFieldParser();
    }


    public Set<QName> getRequestedFields( InputStream in ) {
        Set<QName> set = wrapped.getRequestedFields( in );
        if( set.size() == 0 ) {
            add( set, "creationdate" );
            add( set,"getlastmodified" );
            add( set,"displayname" );
            add( set,"resourcetype" );
            add( set,"getcontenttype" );
            add( set,"getcontentlength" );
            add( set,"getetag" );
        }
        return set;
    }

    private void add( Set<QName> set, String name ) {
        QName qname = new QName( WebDavProtocol.NS_DAV, name);
        set.add( qname );
    }

}
