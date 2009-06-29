package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 *
 */
public class NullSecurityManager implements FsSecurityManager{

    String realm;
    
    public Object authenticate(String user, String password) {
        return user;
    }

    public boolean authorise(Request request, Method method, Auth auth, Resource resource) {
        return true;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public Object getUserByName( String name ) {
        return null;
    }

    
}
