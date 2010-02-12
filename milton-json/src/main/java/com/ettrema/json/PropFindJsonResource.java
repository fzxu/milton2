package com.ettrema.json;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

public class PropFindJsonResource implements GetableResource {
    
    private final PropFindableResource wrappedResource;
    private final JsonPropFindHandler jsonPropFindHandler;
    private final String encodedUrl;

    public PropFindJsonResource( PropFindableResource wrappedResource, JsonPropFindHandler jsonPropFindHandler, String encodedUrl ) {
        super();
        this.wrappedResource = wrappedResource;
        this.encodedUrl = encodedUrl;
        this.jsonPropFindHandler = jsonPropFindHandler;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        //jsonPropFindHandler.sendContent( wrappedResource, encodedUrl, out, range, params, contentType );
        jsonPropFindHandler.sendContent( wrappedResource, "na", out, range, params, contentType );
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
        return Request.Method.PROPFIND.code;
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
