package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.SecurityManager;
import com.ettrema.httpclient.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * Wraps a milton-client File object to adapt it for use as a milton server resource
 *
 * @author brad
 */
public class FileResourceAdapter extends AbstractRemoteAdapter implements FileResource{

    private final com.ettrema.httpclient.File file;

    public FileResourceAdapter(File file, com.ettrema.httpclient.Resource resource, SecurityManager securityManager, String hostName) {
        super(resource, securityManager, hostName);
        this.file = file;
    }
    
    

    @Override
    public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return file.contentType;
    }

    @Override
    public Long getContentLength() {
        return file.contentLength;
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getCreateDate() {
        return file.getCreatedDate();
    }
    
}
