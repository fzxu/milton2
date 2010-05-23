package com.ettrema.http.acl;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.webdav.ResourceTypeHelper;
import com.ettrema.http.AccessControlledResource;
import java.util.List;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author alex
 */
public class AccessControlledResourceTypeHelper implements ResourceTypeHelper {

    private static final Logger log = LoggerFactory.getLogger( AccessControlledResourceTypeHelper.class );
    private final ResourceTypeHelper wrapped;

    public AccessControlledResourceTypeHelper( ResourceTypeHelper wrapped ) {
        log.debug( "AccessControlledResourceTypeHelper constructed :"+wrapped.getClass().getSimpleName() );
        this.wrapped = wrapped;
    }

    public List<QName> getResourceTypes( Resource r ) {
        log.debug( "getResourceTypes: " + r.getClass() );
        List<QName> list = wrapped.getResourceTypes( r );
        if( r instanceof AccessControlledResource ) {
            //TODO: Need to find out what the QNames for accessControlledResources are
            // BM: maybe there isnt one? its only if it should be added to the
            // resource-type property in a PROPFIND response
            //QName qn = new QName( WebDavProtocol.NS_DAV, "collection");
            //list.add(qn);
        }
        return list;
    }

    public List<String> getSupportedLevels( Resource r ) {
        log.trace( "getSupportedLevels" );
        List<String> list = wrapped.getSupportedLevels( r );
        if( r instanceof AccessControlledResource ) {
            list.add( "access-control" );
        }
        return list;
    }
}
