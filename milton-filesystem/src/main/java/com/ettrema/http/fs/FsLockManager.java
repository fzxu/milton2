package com.ettrema.http.fs;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;

/**
 *
 */
public interface  FsLockManager {

    LockResult lock(LockTimeout timeout, LockInfo lockInfo, FsResource resource) throws NotAuthorizedException;

    LockResult refresh(String token, FsResource resource) throws NotAuthorizedException;

    void unlock(String tokenId, FsResource resource) throws NotAuthorizedException;

    LockToken getCurrentToken(FsResource resource);

}
