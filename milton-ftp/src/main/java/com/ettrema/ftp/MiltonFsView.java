package com.ettrema.ftp;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiltonFsView implements FileSystemView {
    private static final Logger log = LoggerFactory.getLogger( MiltonFsView.class );
    Path homePath;
    CollectionResource home;
    Path currentPath;
    CollectionResource current;
    final String host;
    final ResourceFactory resourceFactory;

    public MiltonFsView( String host, Path homePath, CollectionResource current, ResourceFactory resourceFactory ) {
        super();
        this.host = host;
        this.homePath = homePath;
        this.currentPath = homePath;
        this.current = current;
        this.home = current;
        this.resourceFactory = resourceFactory;
    }

    public FtpFile getHomeDirectory() throws FtpException {
        return wrap( homePath, home );
    }

    public FtpFile getWorkingDirectory() throws FtpException {
        return wrap( homePath, current );
    }

    public boolean changeWorkingDirectory( String dir ) throws FtpException {
        log.debug( "cd: " + dir );
        Path p = Path.path( dir );
        ResourceAndPath rp = getResource( p );
        if( rp.resource == null ) {
            log.debug( "not found: " + p );
            return false;
        } else
            if( rp.resource instanceof CollectionResource ) {
                current = (CollectionResource) rp.resource;
                currentPath = p;
                return true;
            } else {
                log.debug( "not a collection: " + rp.resource.getName() );
                return false;
            }
    }

    public FtpFile getFile( String path ) throws FtpException {
        log.debug( "getFile: " + path );
        Path p = Path.path( path );
        ResourceAndPath rp = getResource( p );
        if( rp.resource == null ) {
            log.debug( "returning new file" );
            return new MiltonFtpFile( this, rp.path, this.current, null );
        } else {
            return new MiltonFtpFile( this, rp.path, rp.resource );
        }
    }

    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    public void dispose() {
    }

    public ResourceAndPath getResource( Path p ) throws FtpException {
        log.debug( "getResource: " + p );
        if( p.isRelative() ) {
            p = Path.path( currentPath.toString() + '/' + p.toString() );
            Resource r = resourceFactory.getResource( host, p.toString() );
            return new ResourceAndPath( r, p );
        } else {
            Resource r = resourceFactory.getResource( host, p.toString() );
            return new ResourceAndPath( r, p );
        }
    }

    public FtpFile wrap( Path path, Resource r ) {
        return new MiltonFtpFile( this, path, r );
    }

    /**
     * Represents a resource (possibly null) and an absolute path (never null)
     */
    public static class ResourceAndPath {

        final Resource resource;
        final Path path;

        public ResourceAndPath( Resource r, Path p ) {
            if( p == null ) throw new IllegalArgumentException( "path may not be null" );
            if( p.isRelative() ) throw new IllegalArgumentException( "path must be absolute" );
            this.resource = r;
            this.path = p;
        }
    }
}
