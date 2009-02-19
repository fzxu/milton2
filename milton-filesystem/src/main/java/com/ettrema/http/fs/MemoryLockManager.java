package com.ettrema.http.fs;

import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class MemoryLockManager implements FsLockManager {

    /**
     * maps current locks by the file associated with the resource
     */
    Map<File,CurrentLock> locksByFile;

    Map<String,CurrentLock> locksByToken;

    public MemoryLockManager() {
        locksByFile = new HashMap<File, CurrentLock>();
        locksByToken = new HashMap<String, CurrentLock>();
    }

    public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo, FsResource resource) {
        LockToken currentLock = currentLock(resource);
        if( currentLock != null ) {
            return LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
        }
        LockToken newToken = new LockToken(UUID.randomUUID().toString(),lockInfo, timeout);
        CurrentLock newLock = new CurrentLock(resource.getFile(), newToken);
        locksByFile.put(resource.getFile(), newLock);
        locksByToken.put(newToken.tokenId, newLock);
        return LockResult.success(newToken);
    }

    public synchronized LockResult refresh(String tokenId, FsResource resource) {
        CurrentLock curLock = locksByToken.get(tokenId);
        curLock.token.setFrom(new Date());
        return LockResult.success(curLock.token);
    }

    public synchronized void unlock(String tokenId, FsResource resource) {
        LockToken lockToken = currentLock(resource);
        if( lockToken != null ) {
            removeLock(lockToken);
        }
    }

    private LockToken currentLock(FsResource resource) {
        CurrentLock curLock = locksByFile.get(resource.getFile ());
        if( curLock == null ) return null;
        LockToken token = curLock.token;
        if( token.isExpired() ) {
            removeLock(token);
            return null;
        } else {
            return token;
        }
    }

    private void removeLock(LockToken token) {
        CurrentLock currentLock = locksByToken.get(token);
        locksByFile.remove(currentLock.file);
        locksByToken.remove(currentLock.token.tokenId);
    }

    class CurrentLock {
        final File file;
        final LockToken token;

        public CurrentLock(File file, LockToken token) {
            this.file = file;
            this.token = token;
        }
    }
}
