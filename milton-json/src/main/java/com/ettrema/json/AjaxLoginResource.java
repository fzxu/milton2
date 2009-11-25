package com.ettrema.json;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author brad
 */
public class AjaxLoginResource implements GetableResource{

    private final String name;

    private final GetableResource wrapped;

    public AjaxLoginResource( String name, GetableResource wrapped ) {
        this.name = name;
        this.wrapped = wrapped;
    }



    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // nothing to send
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

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return name;
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
