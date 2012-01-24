package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.httpclient.HttpException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author brad
 */
public abstract class AbstractRemoteAdapter implements Resource, DeletableResource{
    private final com.ettrema.httpclient.Resource resource;
    private final com.bradmcevoy.http.SecurityManager securityManager;
    private final String hostName;

    public AbstractRemoteAdapter(com.ettrema.httpclient.Resource resource, SecurityManager securityManager, String hostName) {
        this.resource = resource;
        this.securityManager = securityManager;
        this.hostName = hostName;
    }

    @Override
    public String getName() {
        return resource.name;
    }
    
    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return securityManager.authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm(hostName);
    }

    @Override
    public Date getModifiedDate() {
        return resource.getModifiedDate();
    }
    
    @Override
    public String checkRedirect(Request request) {
        return null;
    }    
    

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            resource.delete();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
    
    
}
