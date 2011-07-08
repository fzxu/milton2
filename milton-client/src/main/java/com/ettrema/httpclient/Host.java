package com.ettrema.httpclient;

import com.bradmcevoy.common.Path;
import com.ettrema.cache.Cache;
import com.ettrema.cache.MemoryCache;
import com.ettrema.http.DataRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Host extends Folder {

    private static String PROPFIND_XML = "<?xml version=\"1.0\"?>"
            + "<d:propfind xmlns:d='DAV:' xmlns:c='clyde'><d:prop>"
            + "<d:resourcetype/><d:displayname/><d:getcontentlength/><d:creationdate/><d:getlastmodified/><d:iscollection/>"
            + "<d:quota-available-bytes/><d:quota-used-bytes/><c:crc/>"
            + "</d:prop></d:propfind>";
    private static final Logger log = LoggerFactory.getLogger(Host.class);
    public final String server;
    public final int port;
    public final String user;
    public final String password;
    public final String rootPath;
    /**
     * time in milliseconds to be used for all timeout parameters
     */
    private int timeout;
    final HttpClient client;
    public final List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();
    private String propFindXml = PROPFIND_XML;

    static {
//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
//    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");    
    }

    public Host(String server, int port, String user, String password, ProxyDetails proxyDetails) {
        this(server, null, port, user, password, proxyDetails, 30000, null);
    }

    public Host(String server, int port, String user, String password, ProxyDetails proxyDetails, Cache<Folder, List<Resource>> cache) {
        this(server, null, port, user, password, proxyDetails, 30000, cache); // defaul timeout of 30sec
    }

    public Host(String server, String rootPath, int port, String user, String password, ProxyDetails proxyDetails, Cache<Folder, List<Resource>> cache) {
        this(server, rootPath, port, user, password, proxyDetails, 30000, cache); // defaul timeout of 30sec
    }

    public Host(String server, String rootPath, int port, String user, String password, ProxyDetails proxyDetails, int timeout, Cache<Folder, List<Resource>> cache) {
        super((cache != null ? cache : new MemoryCache<Folder, List<Resource>>("resource-cache-default", 50, 20)));
        if (server == null) {
            throw new IllegalArgumentException("host name cannot be null");
        }
        this.rootPath = rootPath;
        this.timeout = timeout;
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        if (user != null) {
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
        }

        if (user != null && user.length() > 0) {
            client.getParams().setAuthenticationPreemptive(true);
        }
        client.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        client.getParams().setSoTimeout(timeout);
        client.getParams().setConnectionManagerTimeout(timeout);
        if (proxyDetails != null) {
            if (proxyDetails.isUseSystemProxy()) {
                System.setProperty("java.net.useSystemProxies", "true");
            } else {
                System.setProperty("java.net.useSystemProxies", "false");
                if (proxyDetails.getProxyHost() != null && proxyDetails.getProxyHost().length() > 0) {
                    HostConfiguration hostConfig = client.getHostConfiguration();
                    hostConfig.setProxy(proxyDetails.getProxyHost(), proxyDetails.getProxyPort());
                    if (proxyDetails.hasAuth()) {
                        client.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyDetails.getUserName(), proxyDetails.getPassword()));
                    }
                }
            }
        }
    }

    /**
     * Finds the resource by iterating through the path parts resolving collections
     * as it goes. If any path component is not founfd returns null
     *
     * @param path
     * @return
     * @throws IOException
     * @throws com.ettrema.httpclient.HttpException
     */
    public Resource find(String path) throws IOException, com.ettrema.httpclient.HttpException {
        if (path == null || path.length() == 0 || path.equals("/")) {
            return this;
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String[] arr = path.split("/");
        return _find(this, arr, 0);

    }

    public static Resource _find(Folder parent, String[] arr, int i) throws IOException, com.ettrema.httpclient.HttpException {
        String childName = arr[i];
        System.out.println("_find: " + childName + " " + i);        
        
        Resource child = parent.child(childName);
        if (i == arr.length - 1) {
            System.out.println("  child: " + child);
            return child;
        } else {
            if (child instanceof Folder) {
                System.out.println("  go to parent");
                return _find((Folder) child, arr, i + 1);
            } else {
                System.out.println("  not found");
                return null;
            }
        }
    }

    public Folder getFolder(String path) throws IOException, com.ettrema.httpclient.HttpException {
        Resource res = find(path);
        if (res instanceof Folder) {
            return (Folder) res;
        } else {
            throw new RuntimeException("Not a folder: " + res.href());
        }
    }

    synchronized PropFindMethod createPropFind(int depth, String href) {
        PropFindMethod m = new PropFindMethod(urlEncode(href));
        m.addRequestHeader(new Header("Depth", depth + ""));
        m.setDoAuthentication(true);
        return m;
    }

    public synchronized int doMkCol(String newUri) throws com.ettrema.httpclient.HttpException {
        notifyStartRequest();
        MkColMethod p = new MkColMethod(urlEncode(newUri));
        try {
            int result = host().client.executeMethod(p);
            if (result == 409) {
                // probably means the folder already exists
                return result;
            }
            Utils.processResultCode(result, newUri);
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            p.releaseConnection();
            notifyFinishRequest();
        }

    }


    public int doPut(Path path, InputStream content, Long contentLength, String contentType) {
        String dest = getHref(path);
        return doPut(dest, content, contentLength, contentType);
    }

    synchronized int doPut(String newUri, InputStream content, Long contentLength, String contentType) {
        log.trace("put: " + newUri);
        notifyStartRequest();
        String s = urlEncode(newUri);
        PutMethod p = new PutMethod(s);
        
        HttpMethodParams params = new HttpMethodParams();
        params.setSoTimeout(timeout);
        p.setParams(params);
        try {
            RequestEntity requestEntity;
            if (contentLength == null) {
                log.trace("no content length");
                requestEntity = new InputStreamRequestEntity(content, contentType);
            } else {
                requestEntity = new InputStreamRequestEntity(content, contentLength, contentType);
            }
            p.setRequestEntity(requestEntity);
            int result = host().client.executeMethod(p);
            return result;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            p.releaseConnection();
            notifyFinishRequest();
        }
    }

    public synchronized int doCopy(String from, String newUri) throws com.ettrema.httpclient.HttpException {
        notifyStartRequest();
        CopyMethod m = new CopyMethod(urlEncode(from), urlEncode(newUri));
        try {
            int res = host().client.executeMethod(m);
            Utils.processResultCode(res, from);
            return res;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }

    }

    public synchronized int doDelete(String href) throws IOException, com.ettrema.httpclient.HttpException {
        notifyStartRequest();
        DeleteMethod m = new DeleteMethod(urlEncode(href));
        try {
            int res = host().client.executeMethod(m);
            Utils.processResultCode(res, href);
            return res;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    public synchronized int doMove(String href, String newUri) throws IOException, com.ettrema.httpclient.HttpException {
        notifyStartRequest();
        MoveMethod m = new MoveMethod(urlEncode(href), urlEncode(newUri));
        try {
            int res = host().client.executeMethod(m);
            Utils.processResultCode(res, href);
            return res;
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }

    }

    public synchronized List<PropFindMethod.Response> doPropFind(String url, int depth) throws IOException, com.ettrema.httpclient.HttpException {
        log.trace("doPropFind: " + url);
        notifyStartRequest();
        PropFindMethod m = createPropFind(depth, url);

        try {
            if (propFindXml != null) {
                RequestEntity requestEntity = new StringRequestEntity(propFindXml, "text/xml", "UTF-8");
                m.setRequestEntity(requestEntity);
            }

            int res = client.executeMethod(m);
            Utils.processResultCode(res, url);
            if (res == 207) {
                return m.getResponses();
            } else {
                return null;
            }
        } catch (NotFoundException e) {
            log.trace("not found: " + url);
            return Collections.EMPTY_LIST;
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    public synchronized void doGet(String url, StreamReceiver receiver,  List<DataRange> rangeList) throws com.ettrema.httpclient.HttpException, Utils.CancelledException {
        notifyStartRequest();
        RangedGetMethod m = new RangedGetMethod(urlEncode(url), rangeList);
        InputStream in = null;
        try {
            int res = client.executeMethod(m);
            Utils.processResultCode(res, url);
            in = m.getResponseBodyAsStream();
            receiver.receive(in);
        } catch (HttpException ex) {
            m.abort();
            throw new GenericHttpException(ex.getReasonCode(), url);
        } catch (Utils.CancelledException ex) {
            m.abort();
            throw ex;
        } catch (IOException ex) {
            m.abort();
            throw new RuntimeException(ex);
        } finally {
            Utils.close(in);
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    public synchronized void options(String path) throws java.net.ConnectException, Unauthorized, UnknownHostException, SocketTimeoutException, IOException, com.ettrema.httpclient.HttpException {
        String url = this.href() + path;
        doOptions(url);
    }

    public synchronized byte[] get(String path) throws com.ettrema.httpclient.HttpException, Utils.CancelledException {
        String url = this.href() + path;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        doGet(url, new StreamReceiver() {

            public void receive(InputStream in) {
                try {
                    IOUtils.copy(in, out);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, null);
        return out.toByteArray();
    }

    private synchronized void doOptions(String url) throws NotFoundException, java.net.ConnectException, Unauthorized, java.net.UnknownHostException, SocketTimeoutException, IOException, com.ettrema.httpclient.HttpException {
        notifyStartRequest();
        String uri = urlEncode(url);
        log.trace("doOptions: {}", url);
        OptionsMethod m = new OptionsMethod(uri);
        InputStream in = null;
        try {
            int res = client.executeMethod(m);
            log.trace("result code: " + res);
            if (res == 301 || res == 302) {
                return;
            }
            Utils.processResultCode(res, url);
        } finally {
            Utils.close(in);
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    /**
     * POSTs the variables and returns the body
     *
     * @param url
     * @param params
     * @return
     */
    String doPost(String url, Map<String, String> params) throws com.ettrema.httpclient.HttpException {
        notifyStartRequest();
        PostMethod m = new PostMethod(urlEncode(url));
        for (Entry<String, String> entry : params.entrySet()) {
            m.addParameter(entry.getKey(), entry.getValue());
        }
        InputStream in = null;
        try {
            int res = client.executeMethod(m);
            Utils.processResultCode(res, url);
            in = m.getResponseBodyAsStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy(in, bout);
            return bout.toString();
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            Utils.close(in);
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    @Override
    public Host host() {
        return this;
    }

    @Override
    public String href() {
        String s = "http://" + server;
        if (this.port != 80) {
            s += ":" + this.port;
        }
        s += "/";
        if (rootPath != null && rootPath.length() > 0) {
            if (!rootPath.equals("/")) {
                s = s + rootPath;
            }
        }
        if (!s.endsWith("/")) {
            s = s + "/";
        }
        //log.trace("host href: " + s);
        return s;
    }

    public String getHref(Path path) {
        String s = "http://" + server;
        if (this.port != 80) {
            s += ":" + this.port;
        }
        s += "/";
        if (rootPath != null && rootPath.length() > 0) {
            if (!rootPath.equals("/")) {
                s = s + rootPath;
            }
        }
        if (s.endsWith("/")) {
            if (!path.isRelative()) {
                s = s.substring(0, s.length() - 1);
            }
        } else {
            if (path.isRelative()) {
                s = s + "/";
            }
        }
        //log.trace("host href: " + s);
        return s + path; // path will be absolute
    }

    public static String urlEncode(String s) {
//        if( rootPath != null ) {
//            s = rootPath + s;
//        }
        return urlEncodePath(s);
    }

    public static String urlEncodePath(String s) {
        try {
            org.apache.commons.httpclient.URI uri = new URI(s, false);
            s = uri.toString();
            s = s.replace("&", "%26");
            return s;
        } catch (URIException ex) {
            throw new RuntimeException(s, ex);
        } catch (NullPointerException ex) {
            throw new RuntimeException(s, ex);
        }
        //s = s.replace( " ", "%20" );
    }

    void notifyStartRequest() {
        for (ConnectionListener l : connectionListeners) {
            l.onStartRequest();
        }
    }

    void notifyFinishRequest() {
        for (ConnectionListener l : connectionListeners) {
            l.onFinishRequest();
        }
    }

    public String getPropFindXml() {
        return propFindXml;
    }

    public void setPropFindXml(String propFindXml) {
        this.propFindXml = propFindXml;
    }

    public com.ettrema.httpclient.Folder getOrCreateFolder(Path remoteParentPath, boolean create) throws com.ettrema.httpclient.HttpException, IOException {
        log.trace("getOrCreateFolder: {}", remoteParentPath);
        com.ettrema.httpclient.Folder f = this;
        if (remoteParentPath != null) {
            for (String childName : remoteParentPath.getParts()) {
                if (childName.equals("_code")) {
                    f = new Folder(f, childName, cache); 
                } else {
                    com.ettrema.httpclient.Resource child = f.child(childName);
                    if (child == null) {
                        if (create) {
                            f = f.createFolder(childName);
                        } else {
                            return null;
                        }
                    } else if (child instanceof com.ettrema.httpclient.Folder) {
                        f = (com.ettrema.httpclient.Folder) child;
                    } else {
                        log.warn("Can't upload. A resource exists with the same name as a folder, but is a file: " + remoteParentPath + " - " + child.getClass());
                        return null;
                    }
                }

            }
        }
        return f;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}

