package com.bradmcevoy.http;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PutHandler extends Handler {
    
    private static final Logger log = LoggerFactory.getLogger(PutHandler.class);
    
    public PutHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    public Request.Method method() {
        return Method.PUT;
    }       
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof PutableResource);
    }        

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, ConflictException {
        String host = request.getHostHeader();
        String urlToCreateOrUpdate = HttpManager.decodeUrl(request.getAbsolutePath());
        String name;
        log.debug("process request: host: " + host + " url: " + urlToCreateOrUpdate);

        Path path = Path.path(urlToCreateOrUpdate);
        urlToCreateOrUpdate = path.toString();

        Resource existingResource = manager.getResourceFactory().getResource(host, urlToCreateOrUpdate);
        ReplaceableResource replacee;
        if( existingResource != null && existingResource instanceof ReplaceableResource ) {
            replacee = (ReplaceableResource) existingResource;
        } else {
            replacee = null;
        }

        if( replacee != null ) {
            processReplace(request,response,(ReplaceableResource)existingResource);
        } else {
            // either no existing resource, or its not replaceable. check for folder
            String urlFolder = path.getParent().toString();
            String nameToCreate = path.getName();
            CollectionResource folderResource = findOrCreateFolders(host, path.getParent());
            if( folderResource != null ) {
                log.debug("found folder: " + urlFolder);
                if( folderResource instanceof PutableResource ) {
                    PutableResource putableResource = (PutableResource) folderResource;
                    processCreate(manager, request, response, (PutableResource)putableResource, nameToCreate);
                } else {
                    manager.getResponseHandler().respondMethodNotImplemented(folderResource, response, request);
                }
            } else {
                response.setStatus(Response.Status.SC_NOT_FOUND);
            }
        }
    }

    protected void processCreate(HttpManager milton, Request request, Response response, PutableResource folder, String newName) {
        log.debug("processCreate: " + newName + " in " + folder.getName());
        if( !checkAuthorisation(folder,request) ) {
            respondUnauthorised(folder,response,request);
            return ;
        }

        log.debug("process: putting to: " + folder.getName() );
        try {
            Long l = request.getContentLengthHeader();
            String ct = request.getContentTypeHeader();
            log.debug("PutHandler: creating resource of type: " + ct);
            folder.createNew(newName, request.getInputStream(), l, ct );
            log.debug("PutHandler: DONE creating resource");
        } catch (IOException ex) {
            log.warn("IOException reading input stream. Probably interrupted upload: " + ex.getMessage());
            return;
        }
        getResponseHandler().respondCreated(folder, response, request);
        
        log.debug("process: finished");
    }

    private CollectionResource findOrCreateFolders( String host, Path parent ) throws NotAuthorizedException, ConflictException {
        Resource root = manager.getResourceFactory().getResource(host, "/");
        if( root == null ) {
            log.debug( "root resource not found");
            return null;
        }
        if( root instanceof CollectionResource) {
            CollectionResource folder = (CollectionResource) root;
            for( String s : parent.getParts()) {
                Resource r = folder.child( s);
                if( r == null ) {
                    if( folder instanceof MakeCollectionableResource) {
                        MakeCollectionableResource mkcol = (MakeCollectionableResource) r;
                        folder = mkcol.createCollection( s );
                    } else {
                        log.debug( "parent folder isnt a MakeCollectionableResource: " + folder.getName());
                        return null;
                    }
                } else if( r instanceof CollectionResource ) {
                    folder = (CollectionResource) r;
                } else {
                    log.debug( "parent in URL is not a collection: " + r.getName());
                    return null;
                }
            }
            return folder;
        } else {
            log.debug( "root is not a collection");
            return null;
        }

    }

    /**
     * "If an existing resource is modified, either the 200 (OK) or 204 (No Content) response codes SHOULD be sent to indicate successful completion of the request."
     * 
     * @param request
     * @param response
     * @param replacee
     */
    private void processReplace(Request request, Response response, ReplaceableResource replacee) {
        if( !checkAuthorisation(replacee,request) ) {
            respondUnauthorised(replacee,response,request);
            return ;
        }

        // TODO: check if locked

        try {
            Long l = request.getContentLengthHeader();
            replacee.replaceContent(request.getInputStream(), l);
            log.debug("PutHandler: DONE creating resource");
        } catch (IOException ex) {
            log.warn("IOException reading input stream. Probably interrupted upload: " + ex.getMessage());
            return;
        }
        getResponseHandler().respondCreated(replacee, response, request);

        log.debug("process: finished");
    }
}