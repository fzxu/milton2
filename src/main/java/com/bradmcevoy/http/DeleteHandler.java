package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeleteHandler extends ExistingEntityHandler {
    
    private Logger log = LoggerFactory.getLogger(DeleteHandler.class);
    
    public DeleteHandler(HttpManager manager) {
        super(manager);
    }
    
    @Override
    protected Request.Method method() {
        return Method.DELETE;
    }    
    
    @Override
    protected boolean isCompatible(Resource handler) {
        return (handler instanceof DeletableResource);
    }        

    @Override
    protected void process(HttpManager milton, Request request, Response response, Resource resource) {
        log.debug("DELETE: " + request.getAbsoluteUrl());
        DeletableResource r = (DeletableResource) resource;
        try {
            delete( r );
            response.setStatus(Response.Status.SC_NO_CONTENT);
            log.debug("deleted ok");
        } catch(CantDeleteException e) {
            log.error("failed to delete: " + request.getAbsoluteUrl(),e);
            response.setStatus(Response.Status.SC_MULTI_STATUS);
            response.setStatus( Response.Status.SC_MULTI_STATUS );
            response.setContentTypeHeader( Response.ContentType.XML.toString() );
            
            String href = request.getAbsoluteUrl();
            
            XmlWriter writer = new XmlWriter( response.getOutputStream() );
            writer.writeXMLHeader();
            writer.open("multistatus" + generateNamespaceDeclarations());
            writer.newLine();
            XmlWriter.Element elResponse = writer.begin("response").open();
                writer.writeProperty("","href",href );
                writer.writeProperty("","status",e.status.code+"" );
            elResponse.close();
            writer.close("multistatus");
            writer.flush();            
        }
        
    }

    private void delete(DeletableResource r) throws CantDeleteException {
        if( r instanceof CollectionResource ) {
            CollectionResource col = (CollectionResource)r;
            List<Resource> list = new ArrayList<Resource>();
            list.addAll( col.getChildren() );
            for( Resource rChild : list ) {
                if( rChild instanceof DeletableResource ) {
                    DeletableResource rChildDel = (DeletableResource)rChild;
                    delete( rChildDel );
                } else {
                    throw new CantDeleteException(rChild, Response.Status.SC_LOCKED);
                }
            }
        }
        r.delete();
    }
    
    public static class CantDeleteException extends Exception {
        
        private static final long serialVersionUID = 1L;
        public final Resource resource;
        public final Response.Status status;
        
        CantDeleteException(Resource r,Response.Status status) {
            this.resource = r;
            this.status = status;
        }
    }
}