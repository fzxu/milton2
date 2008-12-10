package com.bradmcevoy.http;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExistingEntityHandler extends Handler {

    private Logger log = LoggerFactory.getLogger(ExistingEntityHandler.class);


    public ExistingEntityHandler(HttpManager manager) {
        super(manager);
    }

    /** Implement method specific processing. The resource can be safely cast as
     *  the appropriate method specific interface if isCompatible has been implemented
     *  correctly
     */
    protected abstract void process(HttpManager milton, Request request, Response response, Resource resource);

    @Override
    public void process(HttpManager manager, Request request, Response response) {
        String host = request.getHostHeader();
        String url = HttpManager.decodeUrl(request.getAbsolutePath());

        Resource r = manager.getResourceFactory().getResource(host, url);
        if (r != null) {
            processResource(manager, request, response, r);
        } else {            
            respondNotFound(request,response);
        }
    }

    protected void processResource(HttpManager manager, Request request, Response response, Resource resource) {
        long t = System.currentTimeMillis();
        try {
            
            manager.onProcessResourceStart(request, response, resource);

            if (doCheckRedirect(request, response, resource)) {
                return;
            }

            if (!isCompatible(resource)) {
                respondMethodNotAllowed(resource, response);
                return;
            }

            if (!checkAuthorisation(resource, request)) {
                respondUnauthorised(resource, response);
                return;
            }

            process(manager, request, response, resource);
        } finally {
            t = System.currentTimeMillis() - t;
            manager.onProcessResourceFinish(request, response, resource,t);
        }
    }

    /** We generally don't do redirects. Overridden in GetHandler
     *
     *  TODO: refactor this so only those methods who care about it know about it
     */
    protected boolean doCheckRedirect(Request request, Response response, Resource resource) {
        return false;
    }


}
