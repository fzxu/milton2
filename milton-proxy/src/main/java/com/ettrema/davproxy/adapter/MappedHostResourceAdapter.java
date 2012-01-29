package com.ettrema.davproxy.adapter;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.ettrema.httpclient.Folder;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HostBuilder;
import com.ettrema.httpclient.HttpException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Represents a remote DAV host which has been mapped onto the DAV proxy
 *
 * @author brad
 */
public class MappedHostResourceAdapter extends AbstractRemoteAdapter implements CollectionResource, MakeCollectionableResource, PutableResource {

    private final com.ettrema.httpclient.Host remoteHost;
    private final String name;

    public MappedHostResourceAdapter(String name, Host host, com.bradmcevoy.http.SecurityManager securityManager, String hostName) {
        super(host, securityManager, hostName);
        this.remoteHost = host;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
        Folder newRemoteFolder;
        try {
            newRemoteFolder = remoteHost.createFolder(newName);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        }
        return new FolderResourceAdapter(newRemoteFolder, getSecurityManager(), newName);
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


}

