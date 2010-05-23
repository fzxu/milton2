package com.ettrema.http.caldav.demo;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

public class TResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResourceFactory.class );
    public static final TFolderResource ROOT = new TFolderResource( (TFolderResource) null, "http://localhost:7080/caldavdemo" );

    static {
        TFolderResource folder = new TFolderResource( ROOT, "folder1" );
        TCalendarResource cal1 = new TCalendarResource( folder, "cal1" );
    }

    public Resource getResource( String host, String url ) {
        log.debug( "getResource: url: " + url );
        Path path = Path.path( url );
        Resource r = find( path );
        log.debug( "_found: " + r );
        return r;
    }

    private TResource find( Path path ) {
        if( isRoot( path ) ) return ROOT;
        TResource r = find( path.getParent() );
        if( r == null ) return null;
        if( r instanceof TFolderResource ) {
            TFolderResource folder = (TFolderResource) r;
            for( Resource rChild : folder.getChildren() ) {
                TResource r2 = (TResource) rChild;
                if( r2.getName().equals( path.getName() ) ) {
                    return r2;
                } else {
//                    log.debug( "IS NOT: " + r2.getName() + " - " + path.getName());
                }
            }
        }
        log.debug( "not found: " + path );
        return null;
    }

    private boolean isRoot( Path path ) {
        if( path == null ) return true;
        return ( path.getParent() == null || path.getParent().isRoot() );
    }
}
