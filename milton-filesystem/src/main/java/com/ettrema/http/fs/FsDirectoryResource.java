package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a directory in a physical file system.
 *
 */
public class FsDirectoryResource extends FsResource implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, MoveableResource, PropFindableResource, LockingCollectionResource, GetableResource {

    private static final Logger log = LoggerFactory.getLogger(FsDirectoryResource.class);

    public FsDirectoryResource( String host, FileSystemResourceFactory factory, File dir ) {
        super( host, factory, dir );
        if( !dir.exists() )
            throw new IllegalArgumentException( "Directory does not exist: " + dir.getAbsolutePath() );
        if( !dir.isDirectory() )
            throw new IllegalArgumentException( "Is not a directory: " + dir.getAbsolutePath() );
    }

    public CollectionResource createCollection( String name ) {
        File fnew = new File( file, name );
        boolean ok = fnew.mkdir();
        if( !ok )
            throw new RuntimeException( "Failed to create: " + fnew.getAbsolutePath() );
        return new FsDirectoryResource( host, factory, fnew );
    }

    public Resource child( String name ) {
        File fchild = new File( file, name );
        return factory.resolveFile( this.host, fchild );

    }

    public List<? extends Resource> getChildren() {
        ArrayList<FsResource> list = new ArrayList<FsResource>();
        File[] files = this.file.listFiles();
        if( files != null ) {
            for( File fchild : files ) {
                FsResource res = factory.resolveFile( this.host, fchild );
                if( res != null ) {
                    list.add( res );
                } else {
                    log.error("Couldnt resolve file {}", fchild.getAbsolutePath());
                }
            }
        }
        return list;
    }

    /**
     * Will redirect if a default page has been specified on the factory
     *
     * @param request
     * @return
     */
    public String checkRedirect( Request request ) {
        if( factory.getDefaultPage() != null ) {
            return request.getAbsoluteUrl() + "/" + factory.getDefaultPage();
        } else {
            return null;
        }
    }

    public Resource createNew( String name, InputStream in, Long length, String contentType ) throws IOException {
        File dest = new File( this.getFile(), name );
        FileOutputStream out = null;
        try {
            out = new FileOutputStream( dest );
            IOUtils.copy( in, out );
        } finally {
            IOUtils.closeQuietly( out );
        }
        // todo: ignores contentType
        return factory.resolveFile( this.host, dest );

    }

    @Override
    protected void doCopy( File dest ) {
        try {
            FileUtils.copyDirectory( this.getFile(), dest );
        } catch( IOException ex ) {
            throw new RuntimeException( "Failed to copy to:" + dest.getAbsolutePath(), ex );
        }
    }

    public LockToken createAndLock( String name, LockTimeout timeout, LockInfo lockInfo ) throws NotAuthorizedException{
        File dest = new File( this.getFile(), name );
        createEmptyFile( dest );
        FsFileResource newRes = new FsFileResource( host, factory, dest );
        LockResult res = newRes.lock( timeout, lockInfo );
        return res.getLockToken();
    }

    private void createEmptyFile( File file ) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream( file );
        } catch( IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( fout );
        }
    }

    /**
     * Will generate a listing of the contents of this directory, unless
     * the factory's allowDirectoryBrowsing has been set to false.
     *
     * If so it will just output a message saying that access has been disabled.
     *
     * @param out
     * @param range
     * @param params
     * @param contentType
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        XmlWriter w = new XmlWriter( out );
        w.open( "html" );
        w.open( "body" );
        w.begin( "h1" ).open().writeText( this.getName() ).close();
        if( this.factory.isAllowDirectoryBrowsing()) {
            w.open( "table" );
            for( Resource r : getChildren() ) {
                w.open( "tr" );

                w.open( "td" );
                w.begin( "a" ).writeAtt( "href", r.getName() + "/" ).open().writeText( r.getName() ).close();
                w.close( "td" );

                w.begin( "td" ).open().writeText( r.getModifiedDate() + "" ).close();
                w.close( "tr" );
            }
            w.close( "table" );
        } else {
            w.begin( "p").writeText( "Directory browsing is disabled").close();
        }
        w.close( "body" );
        w.close( "html" );
        w.flush();
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public String getContentType( String accepts ) {
        return "text/html";
    }

    public Long getContentLength() {
        return null;
    }

}
