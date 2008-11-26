package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 *
 */
public interface FsSecurityManager {
    Object authenticate(String user, String password);

    boolean authorise(Request request, Method method, Auth auth, Resource resource);
    
    /**
     * 
     * @return - the name of the security realm this is managing
     */
    String getRealm();
}
