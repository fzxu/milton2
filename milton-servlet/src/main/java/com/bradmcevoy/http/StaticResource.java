package com.bradmcevoy.http;

import eu.medsea.util.MimeUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

//import eu.medsea.util.MimeUtil;


/**
 * Used to provide access to static files via Milton
 * 
 * For a full implementation of webdav on a filesystem use the milton-filesysten
 * project
 * 
 * @author brad
 */
public class StaticResource implements GetableResource {
    
    private final File file;
    private String contentType;
    
    public StaticResource(File file, String url, String contentType) {
        if( file.isDirectory() ) throw new IllegalArgumentException("Static resource must be a file, this is a directory: " + file.getAbsolutePath());
        this.file = file;
        this.contentType = contentType;
    }

    public String getUniqueId() {
        return file.hashCode() + "";
    }
    
    public int compareTo(Resource res) {
        return this.getName().compareTo(res.getName());
    }    
    
    public void sendContent(OutputStream out, Range range, Map<String, String> params) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(fis);
        final byte[] buffer = new byte[ 1024 ];
        int n = 0;
        while( -1 != (n = bin.read( buffer )) ) {
            out.write( buffer, 0, n );
        }        
    }

    public String getName() {
        return file.getName();
    }

    public Object authenticate(String user, String password) {
        return "ok";
    }

    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return true;
    }

    public String getRealm() {
        return "ettrema";   //TODO
    }

    public Date getModifiedDate() {        
        Date dt = new Date(file.lastModified());
//        log.debug("static resource modified: " + dt);
        return dt;
    }

    public Long getContentLength() {
        return file.length();
    }

    public String getContentType(String accepts) {
        String s = MimeUtil.getMimeType(file.getAbsolutePath());
        s = MimeUtil.getPreferedMimeType(accepts,s);
        return s;
    }

    public String checkRedirect(Request request) {
        return null;
    }

    public Long getMaxAgeSeconds() {
        return (long)60*60*24;
    }

}
