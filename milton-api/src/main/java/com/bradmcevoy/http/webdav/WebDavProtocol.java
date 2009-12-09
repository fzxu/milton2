package com.bradmcevoy.http.webdav;

import com.bradmcevoy.http.Handler;
import com.bradmcevoy.http.HandlerHelper;
import com.bradmcevoy.http.HttpExtension;
import com.bradmcevoy.http.ResourceHandlerHelper;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author brad
 */
public class WebDavProtocol implements HttpExtension {

    public static final String NS_DAV = "DAV:";

    private final Set<Handler> handlers;

    public WebDavProtocol( Set<Handler> handlers ) {
        this.handlers = handlers;
    }

    public WebDavProtocol(WebDavResponseHandler responseHandler) {
        handlers = new HashSet<Handler>();
        HandlerHelper handlerHelper = new HandlerHelper();
        ResourceHandlerHelper resourceHandlerHelper = new ResourceHandlerHelper( handlerHelper, responseHandler);
        ResourceTypeHelper resourceTypeHelper = new WebDavResourceTypeHelper();
        handlers.add( new PropFindHandler(resourceHandlerHelper, resourceTypeHelper, responseHandler) );
        handlers.add( new MkColHandler(responseHandler, handlerHelper ) );
        handlers.add( new PropPatchHandler(resourceHandlerHelper ) );
        handlers.add( new CopyHandler(responseHandler, handlerHelper, resourceHandlerHelper ) );
        handlers.add( new LockHandler(responseHandler, handlerHelper ) );
        handlers.add( new UnlockHandler(resourceHandlerHelper ) );
        handlers.add( new MoveHandler(responseHandler, handlerHelper, resourceHandlerHelper ) );
    }

    public Set<Handler> getHandlers() {
        return Collections.unmodifiableSet( handlers );
    }

    public static class SupportedLocks {
    }
}
