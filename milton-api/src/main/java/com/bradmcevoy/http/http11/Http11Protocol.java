package com.bradmcevoy.http.http11;

import com.bradmcevoy.http.Handler;
import com.bradmcevoy.http.HttpExtension;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public class Http11Protocol implements HttpExtension{

    private final Set<Handler> handlers;

    public Http11Protocol( Set<Handler> handlers ) {
        this.handlers = handlers;
    }

    public Http11Protocol(Http11ResponseHandler responseHandler) {
        this.handlers = new HashSet<Handler>();
        handlers.add(new OptionsHandler(responseHandler));
        handlers.add(new GetHandler(responseHandler));
        handlers.add(new PostHandler(responseHandler));
        handlers.add(new DeleteHandler(responseHandler));
        handlers.add(new PutHandler(responseHandler));
    }

    public Set<Handler> getHandlers() {
        return handlers;
    }

    public void registerHandlers( Map<String, Handler> methodHandlers ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
