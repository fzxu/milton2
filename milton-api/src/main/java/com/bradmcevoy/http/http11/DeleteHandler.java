package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.exceptions.BadRequestException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

public class DeleteHandler implements ExistingEntityHandler {

    private Logger log = LoggerFactory.getLogger(DeleteHandler.class);
    private final Http11ResponseHandler responseHandler;
    private final HandlerHelper handlerHelper;
    private final ResourceHandlerHelper resourceHandlerHelper;
    private DeleteHelper deleteHelper;

    public DeleteHandler(Http11ResponseHandler responseHandler, HandlerHelper handlerHelper) {
        this.responseHandler = responseHandler;
        this.handlerHelper = handlerHelper;
        this.resourceHandlerHelper = new ResourceHandlerHelper(handlerHelper, responseHandler);
        deleteHelper = new DeleteHelperImpl(handlerHelper);
    }

    public String[] getMethods() {
        return new String[]{Method.DELETE.code};
    }

    @Override
    public boolean isCompatible(Resource handler) {
        return (handler instanceof DeletableResource);
    }

    @Override
    public void process(HttpManager manager, Request request, Response response) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.process(manager, request, response, this);
    }

    public void processResource(HttpManager manager, Request request, Response response, Resource r) throws NotAuthorizedException, ConflictException, BadRequestException {
        resourceHandlerHelper.processResource(manager, request, response, r, this);
    }

    public void processExistingResource(HttpManager manager, Request request, Response response, Resource resource) throws NotAuthorizedException, BadRequestException, ConflictException {
        log.debug("DELETE: " + request.getAbsoluteUrl());

        DeletableResource r = (DeletableResource) resource;

        if (deleteHelper.isLockedOut(request, r)) {
            log.info("Could not delete. Is locked");
            responseHandler.respondDeleteFailed(request, response, r, Status.SC_LOCKED);
            return;
        }

        try {
            deleteHelper.delete(r);
            log.debug("deleted ok");
            responseHandler.respondNoContent(resource, response, request);
        } catch (CantDeleteException e) {
            log.error("failed to delete: " + request.getAbsoluteUrl(), e);
            responseHandler.respondDeleteFailed(request, response, e.resource, e.status);
        }

    }

    public DeleteHelper getDeleteHelper() {
        return deleteHelper;
    }

    public void setDeleteHelper(DeleteHelper deleteHelper) {
        this.deleteHelper = deleteHelper;
    }
    

    public static class CantDeleteException extends Exception {

        private static final long serialVersionUID = 1L;
        public final Resource resource;
        public final Response.Status status;

        public CantDeleteException(Resource r, Response.Status status) {
            this.resource = r;
            this.status = status;
        }
    }
}
