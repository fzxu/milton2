package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.HttpException;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author brad
 */
public abstract class AbstractRemoteAdapter implements Resource, DeletableResource {

    private final com.ettrema.httpclient.Resource resource;
    private final com.bradmcevoy.http.SecurityManager securityManager;
    private final String hostName;

    public AbstractRemoteAdapter(com.ettrema.httpclient.Resource resource, com.bradmcevoy.http.SecurityManager securityManager, String hostName) {
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
        if (request.getMethod().equals(Method.GET)) {
            String url = request.getAbsolutePath();
            if (!url.endsWith("/")) {
                return url + "/";
            }
        }
        return null;
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        try {
            resource.delete();
        } catch (NotFoundException ex) {
            return; // ok, not there to delete
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
    }

    public com.bradmcevoy.http.SecurityManager getSecurityManager() {
        return securityManager;
    }
}
