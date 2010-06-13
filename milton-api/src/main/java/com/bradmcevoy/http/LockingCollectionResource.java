package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * A collection which allows locking "unmapped resources". This means that a LOCK
 * method can effectively create an empty resource which is immediately locked
 * 
 * Implement this in conjunction with LockableResource on child resources to fully support locking.
 *
 * Note that this interface now extends LockableResource because collection resources
 * need to implement both in most cases.
 *
 * If, however, you don't want your collection resources to be lockable, just
 * implement ConditionalCompatibleResource
 * 
 * See - http://www.ettrema.com:8080/browse/MIL-14
 * 
 * @author brad
 */
public interface  LockingCollectionResource extends CollectionResource, LockableResource {
    
    /**
     * Create an empty non-collection resource of the given name and immediately
     *  lock it
     *
     * It is suggested that the implementor have a specific Resource class to act
     * as the lock null resource. You should consider using the LockNullResource
     * interface
     * 
     * @param name - the name of the resource to create
     * @param timeout - in seconds
     * @param lockInfo
     * @return
     */
    public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException;
    
}
