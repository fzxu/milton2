package com.ettrema.json;

import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Just like a PropFindJsonResource, but with digest auth support
 *
 * @author brad
 */
public class DigestPropFindJsonResource extends PropFindJsonResource implements DigestResource{

    private final DigestResource digestResource;

    public DigestPropFindJsonResource( PropFindableResource wrappedResource, JsonPropFindHandler jsonPropFindHandler, String encodedUrl, Long maxAgeSecs ) {
        super(wrappedResource, jsonPropFindHandler, encodedUrl, maxAgeSecs );
        this.digestResource = (DigestResource) wrappedResource;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestResource.authenticate( digestRequest );
    }
}
