package com.bradmcevoy.http;

import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 * webDAV LOCK
 * 
 * @author brad
 */
public interface LockableResource extends Resource {
    /**
     * Lock this resource and return a token
     * 
     * @param timeout - in seconds, or null
     * @param lockInfo
     * @return - a result containing the token representing the lock if succesful,
     * otherwise a failure reason code
     */
    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException;
    
    /**
     * Renew the lock and return new lock info
     * 
     * @param token
     * @return
     */
    public LockResult refreshLock(String token) throws NotAuthorizedException;

    /**
     * If the resource is currently locked, and the tokenId  matches the current
     * one, unlock the resource
     *
     * @param tokenId
     */
    public void unlock(String tokenId) throws NotAuthorizedException;

    /**
     *
     * @return - the current lock, if the resource is locked, or null
     */
    public LockToken getCurrentLock();
}
