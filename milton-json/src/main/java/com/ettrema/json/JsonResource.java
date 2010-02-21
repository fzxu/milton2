package com.ettrema.json;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import java.util.Date;

/**
 * Abstract class to contain common properties
 *
 * @author brad
 */
public abstract class JsonResource {

    private final Resource wrappedResource;
    private final String name;

    public JsonResource( Resource wrappedResource, String name ) {
        this.wrappedResource = wrappedResource;
        this.name = name;
    }



    public Long getMaxAgeSeconds( Auth auth ) {
        return 0L;
    }

    public String getContentType( String accepts ) {
        return "application/json";
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
        return wrappedResource.authenticate( user, password );
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        return wrappedResource.authorise( request, Request.Method.PROPFIND, auth );
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
