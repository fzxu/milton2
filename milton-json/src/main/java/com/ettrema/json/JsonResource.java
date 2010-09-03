package com.ettrema.json;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to contain common properties
 *
 * @author brad
 */
public abstract class JsonResource {

    private static final Logger log = LoggerFactory.getLogger( JsonResource.class );

    private final Resource wrappedResource;
    private final String name;

    public abstract Method applicableMethod();

    public JsonResource( Resource wrappedResource, String name ) {
        this.wrappedResource = wrappedResource;
        this.name = name;
    }



    public Long getMaxAgeSeconds( Auth auth ) {
        return 0L;
    }

    public String getContentType( String accepts ) {
        return "application/x-javascript; charset=utf-8";
        //return "application/json";
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public Object authenticate( String user, String password ) {
        if( log.isDebugEnabled()) {
            log.debug( "authenticate: " + user);
        }
        Object o = wrappedResource.authenticate( user, password );
        if( log.isDebugEnabled()) {
            if( o == null ) {
                log.debug( "authentication failed on wrapped resource of type: " + wrappedResource.getClass());
            }
        }
        return o;
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        boolean b = wrappedResource.authorise( request, applicableMethod(), auth );
        if( log.isDebugEnabled()) {
            if( !b ) {
                log.trace( "authorise failed on wrapped resource of type: " + wrappedResource.getClass());
            } else {
                log.trace("all ok");
            }
        }
        return b;
    }

    public String getRealm() {
        return wrappedResource.getRealm();
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect( Request request ) {
        return null;
    }
}
