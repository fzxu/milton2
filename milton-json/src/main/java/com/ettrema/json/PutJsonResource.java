package com.ettrema.json;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PutJsonResource implements PostableResource {

    private static final Logger log = LoggerFactory.getLogger( PutJsonResource.class );
    private final PutableResource wrapped;
    private final String href;

    public PutJsonResource( PutableResource putableResource, String href ) {
        this.wrapped = putableResource;
        this.href = href;
    }

    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) {
        if( files.isEmpty() ) {
            log.debug( "no files uploaded" );
            return null;
        }
        FileItem file = files.values().iterator().next();
        String newName = file.getName(); // not sure if we always want the original name
        log.debug( "creating resource: " + newName + " size: " + file.getSize() );
        InputStream in = null;
        try {
            in = file.getInputStream();
            wrapped.createNew( newName, in, file.getSize(), file.getContentType() );
            return null;
        } catch( IOException ex ) {
            throw new RuntimeException( "Exception creating resource", ex );
        } finally {
            FileUtils.close( in );
        }
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        // nothing to do
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return null;
    }

    public String getContentType( String accepts ) {
        return null;
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return "PUT";
    }

    public Object authenticate( String user, String password ) {
        return wrapped.authenticate( user, password );
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        return wrapped.authorise( request, method, auth );
    }

    public String getRealm() {
        return wrapped.getRealm();
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect( Request request ) {
        return null;
    }
}
