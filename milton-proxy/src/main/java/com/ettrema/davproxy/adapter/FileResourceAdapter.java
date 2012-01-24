package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.File;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.Utils.CancelledException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * Wraps a milton-client File object to adapt it for use as a milton server resource
 *
 * @author brad
 */
public class FileResourceAdapter extends AbstractRemoteAdapter implements FileResource, ReplaceableResource{

    private final com.ettrema.httpclient.File file;

    public FileResourceAdapter(File file, com.bradmcevoy.http.SecurityManager securityManager, String hostName) {
        super(file, securityManager, hostName);
        this.file = file;
    }
    
    

    @Override
    public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        try {
            file.download(out, null);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (CancelledException ex) {
            throw new RuntimeException(ex);
        }
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

    @Override
    public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
        try {
            file.setContent(in, length);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }

    }
    
}
