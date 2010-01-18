package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 *
 * @author brad
 */
public class ReadOnlySecurityManager implements SecurityManager{

    private final String realm;

    public ReadOnlySecurityManager( String realm ) {
        this.realm = realm;
    }

    public ReadOnlySecurityManager() {
        this.realm = "ReadOnlyRealm";
    }



    public Object authenticate( String user, String password ) {
        return user;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestRequest.getUser();
    }



    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        switch(method) {
            case GET: return true;
            case HEAD: return true;
            case OPTIONS: return true;
            case PROPFIND: return true;
        }
        return false;
    }

    public String getRealm() {
        return realm;
    }

    public Object getUserByName( String name ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

}
