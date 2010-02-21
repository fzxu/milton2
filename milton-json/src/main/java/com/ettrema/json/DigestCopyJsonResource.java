package com.ettrema.json;

import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Just like a PropFindJsonResource, but with digest auth support
 *
 * @author brad
 */
public class DigestCopyJsonResource extends CopyJsonResource implements DigestResource{

    private final DigestResource digestResource;

    public DigestCopyJsonResource( CopyableResource wrappedResource, ResourceFactory resourceFactory ) {
        super(wrappedResource, resourceFactory );
        this.digestResource = (DigestResource) wrappedResource;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestResource.authenticate( digestRequest );
    }
}
