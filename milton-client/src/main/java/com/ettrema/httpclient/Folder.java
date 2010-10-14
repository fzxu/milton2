package com.ettrema.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import com.ettrema.httpclient.PropFindMethod.Response;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Folder extends Resource {

    private static final Logger log = LoggerFactory.getLogger( Folder.class );
    private boolean childrenLoaded = false;
    private final List<Resource> list = new CopyOnWriteArrayList<Resource>();
    final List<FolderListener> folderListeners = new ArrayList<FolderListener>();

    /**
     *  Special constructor for Host
     */
    Folder() {
        super();
    }

    public Folder( Folder parent, Response resp ) {
        super( parent, resp );
    }

    public Folder( Folder parent, String name ) {
        super( parent, name );
    }

    public void addListener( FolderListener l ) throws IOException {
        for( Resource r : this.children() ) {
            l.onChildAdded( r.parent, r );
        }
        folderListeners.add( l );
    }

    public String post( String relativePath, Map<String, String> params ) {
        return host().doPost( href() + relativePath, params );
    }

    @Override
    public File downloadTo( File destFolder, ProgressListener listener ) throws FileNotFoundException, IOException {
        File thisDir = new File( destFolder, this.name );
        thisDir.mkdir();
        for( Resource r : this.children() ) {
            r.downloadTo( thisDir, listener );
        }
        return thisDir;
    }

    public void flush() throws IOException {
        if( list != null ) {
            for( Resource r : list ) {
                notifyOnChildRemoved( r );
            }
            list.clear();
            childrenLoaded = false;
        }
        children();
    }

    public List<? extends Resource> children() throws IOException {
        if( childrenLoaded ) return list;

        List<Response> responses = host().doPropFind( href(), 1 );
        childrenLoaded = true;
        if( responses != null ) {
            log.trace( "responses: {}", responses.size() );
            for( Response resp : responses ) {
                if( !resp.href.equals( this.href() ) ) {
                    Resource r = Resource.fromResponse( this, resp );
                    list.add( r );
                    this.notifyOnChildAdded( r );
                }
            }
        } else {
            log.trace( "null responses" );
        }
        return list;
    }

    public void removeListener( FolderListener folderListener ) {
        this.folderListeners.remove( folderListener );
    }

    @Override
    public String toString() {
        return href() + " (is a folder)";
    }

    public void upload( File f ) throws IOException {
        upload( f, null );
    }

    public void upload( File f, ProgressListener listener ) throws IOException {
        if( f.isDirectory() ) {
            uploadFolder( f, listener );
        } else {
            uploadFile( f, listener );
        }
    }

    protected void uploadFile( File f, ProgressListener listener ) {
        log.warn( "uploadFile: " + f.getAbsolutePath());
        log.trace( "uploadFile: " + listener);
        NotifyingFileInputStream in = null;
        try {
            in = new NotifyingFileInputStream( f, listener );
            upload( f.getName(), in, f.length() );
            flush();
            listener.onComplete( in.fileName );
        } catch( Throwable ex ) {
            throw new RuntimeException( f.getAbsolutePath(), ex );
        } finally {
            Utils.close( in );
        }
    }

    protected void uploadFolder( File folder, ProgressListener listener ) throws IOException {
        if( folder.getName().startsWith( "." ) ) {
            return;
        }
        Folder newFolder = createFolder( folder.getName() );
        for( File f : folder.listFiles() ) {
            newFolder.upload( f, listener );
        }
    }

    public com.ettrema.httpclient.File upload( String name, InputStream content, Long contentLength ) throws IOException {
        children(); // ensure children are loaded
        String newUri = href() + name;
        String contentType = URLConnection.guessContentTypeFromName( name );
        log.trace( "upload: " + newUri );
        host().doPut( newUri, content, contentLength, contentType );
        com.ettrema.httpclient.File child = new com.ettrema.httpclient.File( this, name, contentType, contentLength );
        com.ettrema.httpclient.Resource oldChild = this.child( child.name );
        if( oldChild != null ) {
            this.list.remove( oldChild );
        }
        this.list.add( child );
        notifyOnChildAdded( child );
        return child;
    }

    public Folder createFolder( String name ) throws IOException {
        children(); // ensure children are loaded
        String newUri = href() + name;
        host().doMkCol( newUri );
        Folder child = new Folder( this, name );
        this.list.add( child );
        notifyOnChildAdded( child );
        return child;
    }

    public Resource child( String childName ) throws IOException {
        for( Resource r : children() ) {
            if( r.name.equals( childName ) ) return r;
        }
        return null;
    }

    void notifyOnChildAdded( Resource child ) {
        List<FolderListener> l2 = new ArrayList<FolderListener>( folderListeners );
        for( FolderListener l : l2 ) {
            l.onChildAdded( this, child );
        }
    }

    void notifyOnChildRemoved( Resource child ) {
        List<FolderListener> l2 = new ArrayList<FolderListener>( folderListeners );
        for( FolderListener l : l2 ) {
            l.onChildRemoved( this, child );
        }
    }

    private class NotifyingFileInputStream extends FileInputStream {

        final ProgressListener listener;
        final String fileName;
        long pos;
        long totalLength;

        // the system time we last notified the progress listener
        long timeLastNotify;
        long bytesSinceLastNotify;

        public NotifyingFileInputStream( File f, ProgressListener listener ) throws FileNotFoundException {
            super( f );
            this.listener = listener;
            this.totalLength = f.length();
            this.fileName = f.getAbsolutePath();
            this.timeLastNotify = System.currentTimeMillis();
        }

        @Override
        public int read() throws IOException {
            increment( 1 );
            return super.read();
        }

        @Override
        public int read( byte[] b ) throws IOException {
            increment( b.length );
            return super.read( b );
        }

        @Override
        public int read( byte[] b, int off, int len ) throws IOException {
            increment( len );
            return super.read( b, off, len );
        }

        private void increment( int len ) {
            pos += len;
            notifyListener(len);
        }

        void notifyListener( int numBytes ) {
            bytesSinceLastNotify += numBytes;
            int timeDiff = (int) ( System.currentTimeMillis() - timeLastNotify );
            log.trace( "notify progrss listener: " + timeDiff);
            if( timeDiff > 100 ) {
                timeLastNotify = System.currentTimeMillis();
                int bytesPerSec = (int) ( bytesSinceLastNotify / timeDiff );
                if( totalLength <= 0 ) {
                    listener.onProgress( 100, fileName, bytesPerSec );
                } else {
                    int percent = (int) ( ( pos * 100 / totalLength ) );
                    if( percent > 100 ) percent = 100;
                    listener.onProgress( percent, fileName, bytesPerSec );
                }

            }

        }
    }

    @Override
    public String href() {
        return super.href() + "/";
    }
}
