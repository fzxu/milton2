package com.ettrema.json;

import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.http11.auth.DigestResponse;

/**
 *
 * @author brad
 */
public class DigestPutJsonResource extends PutJsonResource implements DigestResource {

    private final DigestResource digestResource;

    public DigestPutJsonResource( PutableResource putableResource, String href ) {
        super(putableResource, href );
        this.digestResource = (DigestResource) putableResource;
    }

    public Object authenticate( DigestResponse digestRequest ) {
        return digestResource.authenticate( digestRequest );
    }

}
