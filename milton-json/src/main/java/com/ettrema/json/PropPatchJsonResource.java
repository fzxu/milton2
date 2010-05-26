package com.ettrema.json;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class PropPatchJsonResource extends JsonResource implements PostableResource {
    
    private final Resource wrappedResource;
    private final JsonPropPatchHandler patchHandler;
    private final String encodedUrl;

    public PropPatchJsonResource( Resource wrappedResource, JsonPropPatchHandler patchHandler, String encodedUrl ) {
        super(wrappedResource, Request.Method.PROPPATCH.code);
        this.wrappedResource = wrappedResource;
        this.encodedUrl = encodedUrl;
        this.patchHandler = patchHandler;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        out.write("ok".getBytes());
    }

    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException {
        patchHandler.process(wrappedResource, encodedUrl, parameters);
        return null;
    }
}
