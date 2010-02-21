package com.ettrema.json;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards the POST request to the copy method on the wrapped
 * resource, looking up destination collection and name from the "destination" request parameter
 *
 * @author brad
 */
public class CopyJsonResource extends JsonResource implements PostableResource{
    private static final Logger log = LoggerFactory.getLogger( CopyJsonResource.class );
    private final ResourceFactory resourceFactory;
    private final CopyableResource wrapped;

    public CopyJsonResource( CopyableResource copyableResource, ResourceFactory resourceFactory ) {
        super(copyableResource, Request.Method.COPY.code);
        this.wrapped = copyableResource;
        this.resourceFactory = resourceFactory;
    }
    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws BadRequestException, NotAuthorizedException {
        String dest = parameters.get( "destination");
        Path pDest = Path.path( dest );
        Resource rDestParent = resourceFactory.getResource( dest, pDest.getParent().toString());
        if( rDestParent == null ) throw new BadRequestException( wrapped, "The destination parent does not exist");
        if(rDestParent instanceof CollectionResource ) {
            CollectionResource colDestParent = (CollectionResource) rDestParent;
            wrapped.copyTo( colDestParent, pDest.getName());
            return null;
        } else {
            throw new BadRequestException( wrapped, "The destination parent is not a collection resource");
        }
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        // nothing to do
    }

}
