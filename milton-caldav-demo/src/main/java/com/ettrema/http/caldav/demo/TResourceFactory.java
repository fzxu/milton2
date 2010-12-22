package com.ettrema.http.caldav.demo;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

public class TResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResourceFactory.class );
    public static final TFolderResource ROOT = new TFolderResource( (TFolderResource) null, "http://localhost:9080" );

    static {
        TFolderResource principals = new TFolderResource( ROOT, "principals" );
        TFolderResource folder = new TFolderResource( ROOT, "folder1" );
        TFolderResource calendarHome = new TFolderResource( ROOT, "calendarHome" );
        TCalendarResource calendar = new TCalendarResource( calendarHome, "calendarOne" );
    }

    public Resource getResource( String host, String url ) {
        log.debug( "getResource: url: " + url );
        Path path = Path.path( url );
        Resource r = find( path, ROOT );
        log.debug( "_found: " + r + " for url: "+url+" and path: "+path);
        return r;
    }

    private Resource find( Path path, Resource resource) {
        log.debug("Finding resouce : "+path+" in folder : "+resource.getName());
          log.debug("Resource : "+resource.getName());
          log.debug("Path     : "+path.getFirst()+" OR "+path.toString());
          if (resource.getName().equals(path.getFirst())) {
          if( resource instanceof CollectionResource )
          {
            Path remainder = path.getStripFirst();
            for (Resource child : ((CollectionResource)resource).getChildren()) {
              find (remainder, child);
            }
          }
          else if ( resource instanceof TResource)
          {
            return resource;
          }
        }

//        Resource r = find( path.getParent() );
//        if( r == null ) return null;
//        if( r instanceof TFolderResource ) {
//            TFolderResource folder = (TFolderResource) r;
//            for( Resource rChild : folder.getChildren() ) {
//                Resource r2 = (Resource) rChild;
//                if( r2.getName().equals( path.getName() ) ) {
//                    log.debug( "RESOURCE FOUND : " + r2.getName() + " - " + path.getName());
//                    return r2;
//                } else {
//                    log.debug( "IS NOT: " + r2.getName() + " - " + path.getName());
//                }
//            }
//        }
//        log.debug( "not found: " + path );
        return null;
    }

    private boolean isRoot( Path path ) {
        if( path == null ) return true;
        return ( path.getParent() == null );
    }
}
