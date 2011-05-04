package com.ettrema.http.caldav.demo;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * For iCal, start off by opening a calendar at
 *
 * http://localhost:8080/principals/userA/
 *
 * @author brad
 */
public class TResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResourceFactory.class );
    public static final TFolderResource ROOT = new TFolderResource( (TFolderResource) null, "http://localhost:8080" );

    static {
        TFolderResource principals = new TFolderResource( ROOT, "principals" );
        TFolderResource calendarHome = new TFolderResource( ROOT, "calendarHome" );

        TCalendarResource calendar = new TCalendarResource( calendarHome, "calendarOne" );
        TCalDavPrincipal p = new TCalDavPrincipal(principals, "userA", calendarHome, null, null, null);
        principals.children.add(p);
    }

    public Resource getResource( String host, String url ) {
        log.debug( "getResource: url: " + url );
        Path path = Path.path( url );
        Resource r = find( path );
        log.debug( "_found: " + r + " for url: " + url + " and path: " + path );
        return r;
    }

    private Resource find( Path path ) {
        if( path.isRoot() ) {
            return ROOT;
        }
        Resource r = find( path.getParent() );
        if( r == null ) return null;
        if( r instanceof TFolderResource ) {
            TFolderResource folder = (TFolderResource) r;
            for( Resource rChild : folder.getChildren() ) {
                Resource r2 = rChild;
                if( r2.getName().equals( path.getName() ) ) {
                    log.debug( "RESOURCE FOUND : " + r2.getName() + " - " + path.getName() );
                    return r2;
                } else {
                    log.debug( "IS NOT: " + r2.getName() + " - " + path.getName() );
                }
            }
        }
        log.debug( "not found: " + path );
        return null;
    }
}
