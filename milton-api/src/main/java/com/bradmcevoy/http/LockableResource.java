package com.bradmcevoy.http;

/**
 *
 * @author brad
 */
public interface LockableResource extends Resource {
    /**
     * Lock this resource and return a token
     * 
     * @param timeout - in seconds, or null
     * @param lockInfo - in seconds, or null
     * @return - a token representing the lock
     */
    public LockToken lock(LockTimeout timeout, LockInfo lockInfo);
    
    /**
     * Renew the lock and return new lock info
     * 
     * @param token
     * @return
     */
    public LockToken refreshLock(String token);
    
    public void unlock(String tokenId);
}
