package com.ettrema.davproxy.adapter;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.*;
import com.ettrema.common.LogUtils;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HostBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author brad
 */
public class RemoteDavResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(RemoteDavResourceFactory.class);
    private final com.bradmcevoy.http.SecurityManager securityManager;
    private final Map<String,Host> roots;

    public RemoteDavResourceFactory(com.bradmcevoy.http.SecurityManager securityManager, Map<String,HostBuilder> roots) {
        this.securityManager = securityManager;
        this.roots = new ConcurrentHashMap();
        for( Entry<String, HostBuilder> entry : roots.entrySet()) {
            this.roots.put(entry.getKey(), entry.getValue().buildHost());
        }
    }

    @Override
    public Resource getResource(String host, String path) {
        LogUtils.trace(log, "getResource: path:", path);
        Path p = Path.path(path);
        return find(host, p);
    }

    /**
     * Recursive method which walks the parts of the path resolving it to a
     * Resource by using the child method on CollectionResource
     *
     * @param p
     * @return
     */
    private Resource find(String host, Path p) {
        if (p.isRoot()) {
            return new RootFolder(host, roots);
        } else {
            Resource rParent = find(host, p.getParent());
            if (rParent == null) {
                return null;
            } else {
                if (rParent instanceof CollectionResource) {
                    CollectionResource parent = (CollectionResource) rParent;
                    return parent.child(p.getName());
                } else {
                    return null;
                }
            }
        }
    }

    public class RootFolder implements CollectionResource {
        // This is the host name which this resource was resolved on. It is NOT the name
        // of any remote webdav host
        private final String hostName;
        
        /**
         * These are the root folders we will present to end users, which are the
         * remote webdav hosts being accessed.
         */
        private final Map<String,Host> roots;

        public RootFolder(String host, Map<String,Host> roots) {
            this.hostName = host;
            this.roots = roots;
        }

        @Override
        public Resource child(String childName) {
            Host h = roots.get(childName);
            if( h == null ) {
                return null;
            } else {
                return new MappedHostResourceAdapter(childName, h, securityManager, hostName);
            }
        }

        @Override
        public List<? extends Resource> getChildren() {
            List<Resource> list = new ArrayList<Resource>();
            for( Entry<String, Host> root : roots.entrySet()) {
                MappedHostResourceAdapter mappedHost = new MappedHostResourceAdapter(root.getKey(), root.getValue(), securityManager, hostName);
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
    }
}
