package com.bradmcevoy.http;

import com.bradmcevoy.http.http11.DeleteHandler.CantDeleteException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of DeleteHelper
 *
 * It will delegate to the resource if it implements DeletableCollectionResource,
 * otherwise it will walk the collection if its a CollectionResource, and finally
 * will just call handlerHelper.isLockedOut otherwise
 *
 */
public class DeleteHelperImpl implements DeleteHelper {

    private Logger log = LoggerFactory.getLogger(DeleteHelperImpl.class);

    private final HandlerHelper handlerHelper;

    public DeleteHelperImpl(HandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    public boolean isLockedOut(Request req, DeletableResource r) {
        if (r instanceof DeletableCollectionResource) {
            DeletableCollectionResource dcr = (DeletableCollectionResource) r;
            boolean locked = dcr.isLockedOutRecursive(req);
            if( locked && log.isInfoEnabled()) {
                log.info("isLocked, as reported by DeletableCollectionResource: " + dcr.getName());
            }
            return locked;
            
        } else if (r instanceof CollectionResource) {
            CollectionResource col = (CollectionResource) r;
            List<Resource> list = new ArrayList<Resource>();
            list.addAll(col.getChildren());
            for (Resource rChild : list) {
                if (rChild instanceof DeletableResource) {
                    DeletableResource rChildDel = (DeletableResource) rChild;
                    if (isLockedOut(req, rChildDel)) {
                        if( log.isInfoEnabled()) {
                            log.info("isLocked: " + rChild.getName() + " type:" + rChild.getClass());
                        }
                        return true;
                    }
                } else {
                    if( log.isInfoEnabled() ) {
                        log.info("a child resource is not deletable: " + rChild.getName() + " type: " + rChild.getClass());
                    }
                    return true;
                }
            }
            return false;

        } else {
            boolean locked = handlerHelper.isLockedOut(req, r);
            if( locked && log.isInfoEnabled()) {
                log.info("isLocked, as reported by handlerHelper on resource: " + r.getName());
            }
            return locked;
            
        }
    }

    public void delete(DeletableResource r) throws CantDeleteException {
        if (r instanceof DeletableCollectionResource) {
            r.delete();

        } else if (r instanceof CollectionResource) {
            CollectionResource col = (CollectionResource) r;
            List<Resource> list = new ArrayList<Resource>();
            list.addAll(col.getChildren());
            for (Resource rChild : list) {
                if (rChild instanceof DeletableResource) {
                    DeletableResource rChildDel = (DeletableResource) rChild;
                    delete(rChildDel);
                } else {
                    throw new CantDeleteException(rChild, Response.Status.SC_LOCKED);
                }
            }
            r.delete();
            
        } else {
            r.delete();
        }
    }
}
