package com.ettrema.httpclient;

import com.bradmcevoy.http.DateUtils;
import com.bradmcevoy.http.DateUtils.DateParseException;
import java.util.ArrayList;
import java.util.List;
import com.ettrema.httpclient.PropFindMethod.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public abstract class Resource {

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    static Resource fromResponse(Folder parent, Response resp) {
        if (resp.isCollection) {
            return new Folder(parent, resp);
        } else {
            return new com.ettrema.httpclient.File(parent, resp);
        }
    }

    /**
     * does percentage decoding on a path portion of a url
     *
     * E.g. /foo  > /foo
     * /with%20space -> /with space
     *
     * @param href
     */
    public static String decodePath(String href) {
        // For IPv6
        href = href.replace("[", "%5B").replace("]", "%5D");

        // Seems that some client apps send spaces.. maybe..
        href = href.replace(" ", "%20");
        // ok, this is milton's bad. Older versions don't encode curly braces
        href = href.replace("{", "%7B").replace("}", "%7D");
        try {
            if (href.startsWith("/")) {
                URI uri = new URI("http://anything.com" + href);
                return uri.getPath();
            } else {
                URI uri = new URI("http://anything.com/" + href);
                String s = uri.getPath();
                return s.substring(1);
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    public Folder parent;
    public String name;
    public String displayName;
    private Date modifiedDate;
    private Date createdDate;
    private final Long quotaAvailableBytes;
    private final Long quotaUsedBytes;
    private final Long crc;
    final List<ResourceListener> listeners = new ArrayList<ResourceListener>();

    public abstract java.io.File downloadTo(java.io.File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, Utils.CancelledException;

    /**
     *  Special constructor for Host
     */
    Resource() {
        this.parent = null;
        this.name = "";
        this.displayName = "";
        this.createdDate = null;
        this.modifiedDate = null;
        quotaAvailableBytes = null;
        quotaUsedBytes = null;
        crc = null;
    }

    public Resource(Folder parent, Response resp) {
        try {
            if (parent == null) {
                throw new NullPointerException("parent");
            }
            this.parent = parent;
            name = Resource.decodePath(resp.name);
            displayName = Resource.decodePath(resp.displayName);
            createdDate = DateUtils.parseWebDavDate(resp.createdDate);
            quotaAvailableBytes = resp.quotaAvailableBytes;
            quotaUsedBytes = resp.quotaUsedBytes;
            crc = resp.crc;

            if (StringUtils.isEmpty(resp.modifiedDate)) {
                modifiedDate = null;
            } else if (resp.modifiedDate.endsWith("Z")) {
                modifiedDate = DateUtils.parseWebDavDate(resp.modifiedDate);
                if (resp.serverDate != null) {
                    // calc difference and use that as delta on local time
                    Date serverDate = DateUtils.parseDate(resp.serverDate);
                    long delta = serverDate.getTime() - modifiedDate.getTime();
                    modifiedDate = new Date(System.currentTimeMillis() - delta);
                } else {
                    log.debug("no server date");
                }
            } else {
                modifiedDate = DateUtils.parseDate(resp.modifiedDate);
            }
            //log.debug( "parsed mod date: " + modifiedDate);
        } catch (DateParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Resource(Folder parent, String name, String displayName, String href, Date modifiedDate, Date createdDate) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.modifiedDate = modifiedDate;
        this.createdDate = createdDate;
        quotaAvailableBytes = null;
        quotaUsedBytes = null;
        crc = null;
    }

    public Resource(Folder parent, String name) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
        this.name = name;
        this.displayName = name;
        this.modifiedDate = null;
        this.createdDate = null;
        quotaAvailableBytes = null;
        quotaUsedBytes = null;
        crc = null;
    }

    public void addListener(ResourceListener l) {
        listeners.add(l);
    }

    public String post(Map<String, String> params) throws HttpException {
        return host().doPost(href(), params);
    }

    public void copyTo(Folder folder) throws IOException, HttpException {
        host().doCopy(href(), folder.href() + this.name);
        folder.flush();
    }

    public void rename(String newName) throws IOException, HttpException {
        String dest = "";
        if (parent != null) {
            dest = parent.href();
        }
        dest = dest + newName;
        int res = host().doMove(href(), dest);
        if (res == 201) {
            this.name = newName;
        }
    }

    public void moveTo(Folder folder) throws IOException, HttpException {
        log.info("Move: " + this.href() + " to " + folder.href());
        int res = host().doMove(href(), folder.href() + this.name);
        if (res == 201) {
            this.parent.flush();
            folder.flush();
        }
    }

    public void removeListener(ResourceListener l) {
        listeners.remove(l);
    }


    @Override
    public String toString() {
        return href() + "(" + displayName + ")";
    }

    public void delete() throws IOException, HttpException {
        host().doDelete(href());
        notifyOnDelete();
    }

    void notifyOnDelete() {
        if (this.parent != null) {
            this.parent.notifyOnChildRemoved(this);
        }
        List<ResourceListener> l2 = new ArrayList<ResourceListener>(listeners);
        for (ResourceListener l : l2) {
            l.onDeleted(this);
        }
    }

    public Host host() {
        Host h = parent.host();
        if (h == null) {
            throw new NullPointerException("no host");
        }
        return h;
    }

//    private String encodedName() {
//        return com.bradmcevoy.http.Utils.percentEncode( name );
//    }
    /**
     * Returns the UN encoded url
     * 
     * @return
     */
    public String href() {
        if (parent == null) {
            return name;
            //return encodedName();
        } else {
            //return parent.href() + encodedName();
            return parent.href() + name;
        }
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Long getQuotaAvailableBytes() {
        return quotaAvailableBytes;
    }

    public Long getQuotaUsedBytes() {
        return quotaUsedBytes;
    }

    public Long getCrc() {
        return crc;
    }
}
