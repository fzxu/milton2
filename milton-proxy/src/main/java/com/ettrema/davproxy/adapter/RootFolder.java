
package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.davproxy.content.FolderHtmlContentGenerator;
import com.ettrema.httpclient.Host;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author brad
 */
public class RootFolder implements CollectionResource, GetableResource, DigestResource, PropFindableResource {
    // This is the host name which this resource was resolved on. It is NOT the name
    // of any remote webdav host
    // This is the host name which this resource was resolved on. It is NOT the name
    // of any remote webdav host
    private final String hostName;
    /**
     * These are the root folders we will present to end users, which are the
     * remote webdav hosts being accessed.
     */
    /**
     * These are the root folders we will present to end users, which are the
     * remote webdav hosts being accessed.
     */
    private final Map<String, Host> roots;
    private final RemoteDavResourceFactory davResourceFactory;
    private final FolderHtmlContentGenerator contentGenerator;
    private final com.bradmcevoy.http.SecurityManager securityManager;
    private final RemoteManager remoteManager;

    public RootFolder(String host, Map<String, Host> roots, final RemoteDavResourceFactory davResourceFactory, FolderHtmlContentGenerator contentGenerator, com.bradmcevoy.http.SecurityManager securityManager, RemoteManager remoteManager) {
        this.davResourceFactory = davResourceFactory;
        this.contentGenerator = contentGenerator;
        this.securityManager = securityManager;
        this.remoteManager = remoteManager;
        this.hostName = host;
        this.roots = roots;
    }

    @Override
    public Resource child(String childName) {
        Host h = roots.get(childName);
        if (h == null) {
            return null;
        } else {
            return new MappedHostResourceAdapter(childName, h, securityManager, hostName, contentGenerator, remoteManager);
        }
    }

    @Override
    public List<? extends Resource> getChildren() {
        List<Resource> list = new ArrayList<Resource>();
        for (Entry<String, Host> root : roots.entrySet()) {
            MappedHostResourceAdapter mappedHost = new MappedHostResourceAdapter(root.getKey(), root.getValue(), securityManager, hostName, contentGenerator, remoteManager);
            list.add(mappedHost);
        }
        return list;
    }

    @Override
    public String getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Object authenticate(String user, String password) {
        return securityManager.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return securityManager.authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return securityManager.getRealm(hostName);
    }

    @Override
    public Date getModifiedDate() {
        return null;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
        String uri = HttpManager.request().getAbsolutePath();
        contentGenerator.generateContent(this, out, uri);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return "text/html";
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return securityManager.authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return true;
    }

    @Override
    public Date getCreateDate() {
        return null;
    }
    
}
