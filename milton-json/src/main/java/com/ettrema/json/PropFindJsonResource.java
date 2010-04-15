package com.ettrema.json;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class PropFindJsonResource extends JsonResource implements GetableResource {
    
    private final PropFindableResource wrappedResource;
    private final JsonPropFindHandler jsonPropFindHandler;
    private final String encodedUrl;

    public PropFindJsonResource( PropFindableResource wrappedResource, JsonPropFindHandler jsonPropFindHandler, String encodedUrl ) {
        super(wrappedResource, Request.Method.PROPFIND.code);
        this.wrappedResource = wrappedResource;
        this.encodedUrl = encodedUrl;
        this.jsonPropFindHandler = jsonPropFindHandler;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        //jsonPropFindHandler.sendContent( wrappedResource, encodedUrl, out, range, params, contentType );
        jsonPropFindHandler.sendContent( wrappedResource, encodedUrl, out, range, params, contentType );
    }
}
