package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.Http11Protocol;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.http.webdav.WebDavResponseHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author brad
 */
public class ProtocolHandlers implements Iterable<HttpExtension>{
    private final List<HttpExtension> handlers;

    public ProtocolHandlers( List<HttpExtension> handlers ) {
        this.handlers = handlers;
    }

    public ProtocolHandlers(WebDavResponseHandler responseHandler) {
        this.handlers = new ArrayList<HttpExtension>();
        this.handlers.add( new Http11Protocol(responseHandler));
        this.handlers.add( new WebDavProtocol(responseHandler));
    }

    public ProtocolHandlers() {
        this.handlers = new ArrayList<HttpExtension>();
        WebDavResponseHandler responseHandler = new DefaultWebDavResponseHandler();
        this.handlers.add( new Http11Protocol(responseHandler));
        this.handlers.add( new WebDavProtocol(responseHandler));
    }

    public Iterator<HttpExtension> iterator() {
        return handlers.iterator();
    }
}
