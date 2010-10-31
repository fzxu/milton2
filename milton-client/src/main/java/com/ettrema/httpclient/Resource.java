package com.ettrema.httpclient;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.DateUtils.DateParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import com.ettrema.httpclient.PropFindMethod.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Resource {

    private static final Logger log = LoggerFactory.getLogger( Resource.class );

    static Resource fromResponse( Folder parent, Response resp ) {
        if( resp.isCollection ) {
            return new Folder( parent, resp );
        } else {
            return new com.ettrema.httpclient.File( parent, resp );
        }
    }

    /**
     * does percentage decoding on a path portion of a url
     *
     * E.g. /foo  > /foo
     * /with%20space -> /with space
     *
     * @param href
     */
    public static String decodePath( String href ) {
        // For IPv6
        href = href.replace( "[", "%5B" ).replace( "]", "%5D" );

        // Seems that some client apps send spaces.. maybe..
        href = href.replace( " ", "%20" );
        // ok, this is milton's bad. Older versions don't encode curly braces
        href = href.replace( "{", "%7B" ).replace( "}", "%7D" );
        try {
            if( href.startsWith( "/" ) ) {
                URI uri = new URI( "http://anything.com" + href );
                return uri.getPath();
            } else {
                URI uri = new URI( "http://anything.com/" + href );
                String s = uri.getPath();
                return s.substring( 1 );
            }
        } catch( URISyntaxException ex ) {
            throw new RuntimeException( ex );
        }
    }
    public Folder parent;
    public String name;
    public String displayName;
    private Date modifiedDate;
    private Date createdDate;
    private final Long quotaAvailableBytes;
    private final Long quotaUsedBytes;
    private final Long crc;
    final List<ResourceListener> listeners = new ArrayList<ResourceListener>();

    /**
     *  Special constructor for Host
     */
    Resource() {
        this.parent = null;
        this.name = "";
        this.displayName = "";
        this.createdDate = null;
        this.modifiedDate = null;
        quotaAvailableBytes = null;
        quotaUsedBytes = null;
        crc = null;
    }

    public Resource( Folder parent, Response resp ) {
        try {
            if( parent == null ) throw new NullPointerException( "parent" );
            this.parent = parent;
            name = Resource.decodePath( resp.name );
            displayName = Resource.decodePath( resp.displayName );
            createdDate = DateUtils.parseWebDavDate( resp.createdDate );
            quotaAvailableBytes = resp.quotaAvailableBytes;
            quotaUsedBytes = resp.quotaUsedBytes;
            crc = resp.crc; 

            if( resp.modifiedDate.endsWith( "Z" ) ) {
                modifiedDate = DateUtils.parseWebDavDate( resp.modifiedDate );
                if( resp.serverDate != null ) {
                    // calc difference and use that as delta on local time
                    Date serverDate = DateUtils.parseDate( resp.serverDate );
                    long delta = serverDate.getTime() - modifiedDate.getTime();
                    modifiedDate = new Date( System.currentTimeMillis() - delta );
                } else {
                    log.debug( "no server date" );
                }
            } else {
                modifiedDate = DateUtils.parseDate( resp.modifiedDate );
            }
            //log.debug( "parsed mod date: " + modifiedDate);
        } catch( DateParseException ex ) {
            throw new RuntimeException( ex );
        }
    }

    public Resource( Folder parent, String name, String displayName, String href, Date modifiedDate, Date createdDate ) {
        if( parent == null ) throw new NullPointerException( "parent" );
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.modifiedDate = modifiedDate;
        this.createdDate = createdDate;
        quotaAvailableBytes = null;
        quotaUsedBytes = null;
        crc = null;
    }

    public Resource( Folder parent, String name ) {
        if( parent == null ) throw new NullPointerException( "parent" );
        this.parent = parent;
        this.name = name;
        this.displayName = name;
        this.modifiedDate = null;
        this.createdDate = null;
        quotaAvailableBytes = null;
        quotaUsedBytes = null;
        crc = null;
    }

    public void addListener( ResourceListener l ) {
        listeners.add( l );
    }

    public String post( Map<String, String> params ) {
        return host().doPost( href(), params );
    }

    public void copyTo( Folder folder ) throws IOException {
        host().doCopy( href(), folder.href() + this.name );
        folder.flush();
    }

    public void rename( String newName ) throws IOException {
        String dest = "";
        if( parent != null ) {
            dest = parent.href();
        }
        dest = dest + newName;
        int res = host().doMove( href(), dest );
        if( res == 201 ) {
            this.name = newName;
        }
    }

    public void moveTo( Folder folder ) throws IOException {
        int res = host().doMove( href(), folder.href() + this.name );
        if( res == 201 ) {
            this.parent.flush();
            folder.flush();
        }
    }

    public void removeListener( ResourceListener l ) {
        listeners.remove( l );
    }

    public File downloadTo( File destFolder, ProgressListener listener ) throws FileNotFoundException, IOException {
        if( !destFolder.exists() )
            throw new FileNotFoundException( destFolder.getAbsolutePath() );
        File dest = new File( destFolder, name );
        return downloadToFile( dest, listener );
    }

    public File downloadToFile( File destFile, ProgressListener listener ) throws FileNotFoundException {
        if( destFile.exists() )
            throw new RuntimeException( "file already exists: " + destFile.getAbsolutePath() );
        File dest = destFile;
        final FileOutputStream out = new FileOutputStream( dest );
        download( out, listener );
        return dest;
    }

    public void download( final OutputStream out, ProgressListener listener ) {
        if( listener != null ) {
            listener.onProgress( 0, this.name );
        }
        try {
            host().doGet( href(), new StreamReceiver() {

                public void receive( InputStream in ) {
                    try {
                        Utils.write( in, out );
                    } catch( IOException ex ) {
                        throw new RuntimeException( ex );
                    }
                }
            } );
        } finally {
            Utils.close( out );
        }
        if( listener != null ) {
            listener.onProgress( 100, this.name );
            listener.onComplete( this.name );
        }
    }

    @Override
    public String toString() {
        return href() + "(" + displayName + ")";
    }

    public void delete() throws IOException {
        host().doDelete( href() );
        notifyOnDelete();
    }

    void notifyOnDelete() {
        if( this.parent != null ) {
            this.parent.notifyOnChildRemoved( this );
        }
        List<ResourceListener> l2 = new ArrayList<ResourceListener>( listeners );
        for( ResourceListener l : l2 ) {
            l.onDeleted( this );
        }
    }

    public Host host() {
        Host h = parent.host();
        if( h == null ) throw new NullPointerException( "no host" );
        return h;
    }

    private String encodedName() {
        return com.bradmcevoy.http.Utils.percentEncode( name );
    }

    public String href() {
        if( parent == null ) {
            return encodedName();
        } else {
            return parent.href() + encodedName();
        }
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Long getQuotaAvailableBytes() {
        return quotaAvailableBytes;
    }

    public Long getQuotaUsedBytes() {
        return quotaUsedBytes;
    }

    public Long getCrc() {
        return crc;
    }

    
}
