package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.Resource;
import java.util.Arrays;
import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class WebDavResourceTypeHelper implements ResourceTypeHelper {

    public List<QName> getResourceTypes( Resource r ) {
        if( r instanceof CollectionResource ) {
            QName qn = new QName( WebDavProtocol.NS_DAV, "collection");
            return Arrays.asList( qn );
        } else {
            return null;
        }
    }

    public List<String> getSupportedLevels( Resource r ) {
        if( r instanceof LockableResource ) {
            return Arrays.asList( "1", "2" );
        } else {
            return Arrays.asList( "1" );
        }
    }
}
