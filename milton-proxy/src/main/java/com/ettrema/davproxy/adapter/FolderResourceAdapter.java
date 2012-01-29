package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.davproxy.content.FolderHtmlContentGenerator;
import com.ettrema.httpclient.Folder;
import com.ettrema.httpclient.HttpException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brad
 */
public class FolderResourceAdapter extends AbstractRemoteAdapter implements FolderResource {

    private final com.ettrema.httpclient.Folder folder;

    private final FolderHtmlContentGenerator contentGenerator;
    
    public FolderResourceAdapter(Folder folder, com.bradmcevoy.http.SecurityManager securityManager, String hostName, FolderHtmlContentGenerator contentGenerator) {
        super(folder, securityManager, hostName);
        this.folder = folder;
        this.contentGenerator = contentGenerator;
    }

    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Folder newRemoteFolder;
        try {
            newRemoteFolder = folder.createFolder(newName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
        return new FolderResourceAdapter(newRemoteFolder, getSecurityManager(), newName, contentGenerator);
    }

    @Override
    public com.bradmcevoy.http.Resource child(String childName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends com.bradmcevoy.http.Resource> getChildren() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public com.bradmcevoy.http.Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copyTo(CollectionResource toCollection, String name) throws NotAuthorizedException, BadRequestException, ConflictException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        String uri = HttpManager.request().getAbsolutePath();
        contentGenerator.generateContent(this, out, uri);
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    @Override
    public String getContentType(String accepts) {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getCreateDate() {
        return folder.getCreatedDate();
    }
}
