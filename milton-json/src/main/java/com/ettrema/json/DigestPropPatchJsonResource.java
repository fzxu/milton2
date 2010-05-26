package com.ettrema.json;

import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Just like a PropFindJsonResource, but with digest auth support
 *
 * @author brad
 */
public class DigestPropPatchJsonResource extends PropPatchJsonResource implements DigestResource{

    private final DigestResource digestResource;

    public DigestPropPatchJsonResource( Resource wrappedResource, JsonPropPatchHandler jsonPropPatchHandler, String encodedUrl ) {
        super(wrappedResource, jsonPropPatchHandler, encodedUrl );
        this.digestResource = (DigestResource) wrappedResource;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestResource.authenticate( digestRequest );
    }
}
