package com.ettrema.json;

import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 * Just like a PropFindJsonResource, but with digest auth support
 *
 * @author brad
 */
public class DigestMkcolJsonResource extends MkcolJsonResource implements DigestResource{

    private final DigestResource digestResource;

    public DigestMkcolJsonResource( MakeCollectionableResource wrappedResource, String encodedUrl ) {
        super(wrappedResource, encodedUrl );
        this.digestResource = (DigestResource) wrappedResource;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestResource.authenticate( digestRequest );
    }
}
