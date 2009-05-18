package com.bradmcevoy.http;

import com.bradmcevoy.http.PropPatchHandler.Fields;
import com.bradmcevoy.http.Request.Method;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class TResource implements GetableResource, PropFindableResource, DeletableResource, MoveableResource, CopyableResource, PropPatchableResource, LockableResource{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResource.class);
    
    String name;
    Date modDate;
    Date createdDate;
    TFolderResource parent;            
    TLock lock;        
    private String user;
    private String password;
    private Map<String,String> props = new HashMap<String, String>();

    protected abstract Object clone(TFolderResource newParent);

    public TResource(TFolderResource parent, String name) {
        this.parent = parent;
        this.name = name;
        modDate = new Date();
        createdDate = new Date();
        if( parent != null ) {
            checkAndRemove(parent,name);
            parent.children.add(this);
        }
        props.put( "someField", "hash:" + this.hashCode() );
    }

    public void setSecure(String user, String password) {
        this.user = user;
        this.password = password;
    }
    
    public String getHref() {
        if( parent == null ) {
            return "/webdav/";
        } else {
            String s = parent.getHref();
            if( !s.endsWith("/") ) s = s + "/";
            s = s + name;
            if( this instanceof CollectionResource ) s = s + "/";
            return s;
        }
    }
    

    public Long getContentLength() {
        return null;
    }


    public String checkRedirect(Request request) {
        return null;
    }


    public Long getMaxAgeSeconds(Auth auth) {
        return (long)10;
    }

    public void moveTo(CollectionResource rDest, String name) {
        log.debug("moving..");
        TFolderResource d = (TFolderResource) rDest;
        this.parent.children.remove(this);
        this.parent = d;
        this.parent.children.add(this);
        this.name = name;
    }
        
    public Date getCreateDate() {
        return createdDate;
    }    
    
    public String getName() {
        return name;
    }

    public Object authenticate(String user, String password) {
        if( this.user == null ) return true;
        return  ( user.equals(this.user)) && (password != null && password.equals(this.password));
    }

    public boolean authorise(Request request, Method method, Auth auth) {
        if( auth == null ) {
            if( this.user == null ) {
                return true;
            } else {
                return false;
            }
        } else {
            return ( this.user == null || auth.user.equals(this.user));
        }
    }

    public String getRealm() {
        return "mockRealm";
    }

    public Date getModifiedDate() {
        return modDate;
    }    
    
    public void delete() {
        if( this.parent == null ) throw new RuntimeException("attempt to delete root");
        if( this.parent.children == null ) throw new NullPointerException("children is null");
        this.parent.children.remove(this);
    }
    
    public void copyTo(CollectionResource toCollection, String name) {
        TResource rClone;
        rClone = (TResource) this.clone((TFolderResource)toCollection);
        rClone.name = name;
    }     
    
    public int compareTo(Resource o) {
        if( o instanceof TResource ) {
            TResource res = (TResource)o;
            return this.getName().compareTo(res.getName());
        } else {
            return -1;
        }
    }

    public String getUniqueId() {
        return this.hashCode()+"";
    }

    public LockToken getCurrentLock() {
        if( this.lock == null ) return null;
        LockToken token = new LockToken();
        token.info = this.lock.lockInfo;
        token.timeout = new LockTimeout(this.lock.seconds);
        token.tokenId = this.lock.lockId;
        return token;
    }



    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) {
//        if( lock != null ) {
//            // todo
//            throw new RuntimeException("already locked");
//        }
                
        LockTimeout.DateAndSeconds lockedUntil = timeout.getLockedUntil(60l, 3600l);
        this.lock = new TLock(lockedUntil.date, UUID.randomUUID().toString(), lockedUntil.seconds, lockInfo);
        
        LockToken token = new LockToken();
        token.info = lockInfo;
        token.timeout = new LockTimeout(lockedUntil.seconds);
        token.tokenId = this.lock.lockId;
                        
        return LockResult.success(token);
    }

    public LockResult refreshLock(String token) {
        if( lock == null ) throw new RuntimeException("not locked");
        if( !lock.lockId.equals(token)) throw new RuntimeException("invalid lock id");
        this.lock = lock.refresh();
        LockToken tok = makeToken();
        return LockResult.success(tok);
    }

    public void unlock(String tokenId) {
        if( lock == null ) {
            log.warn("request to unlock not locked resource");
            return ;
        }
        if( !lock.lockId.equals(tokenId) ) throw new RuntimeException("Invalid lock token");
        this.lock = null;
    }

    LockToken makeToken() {
        LockToken token = new LockToken();
        token.info = lock.lockInfo;
        token.timeout = new LockTimeout(lock.seconds);
        token.tokenId = lock.lockId;
        return token;
    }

    private void checkAndRemove(TFolderResource parent, String name) {
        Resource r = parent.child(name);
        if( r != null ) parent.children.remove(r);
    }

    public void setProperties(Fields fields) {
        for( PropPatchHandler.SetField f : fields.setFields ) {
            props.put(f.name, f.value);
        }
        for( PropPatchHandler.Field f : fields.removeFields ) {
            props.remove(f.name);
        }
    }

    protected void print(PrintWriter printer, String s) {
        printer.print(s);
    }

    class TLock {
        final Date lockedUntil;
        final String lockId;
        final long seconds;
        final LockInfo lockInfo;

        public TLock(Date lockedUntil, String lockId, long seconds, LockInfo lockInfo) {
            this.lockedUntil = lockedUntil;
            this.lockId = lockId;
            this.seconds = seconds;
            this.lockInfo = lockInfo;
        }

        TLock refresh() {
            Date dt = Utils.addSeconds(Utils.now(), seconds);
            return new TLock(dt, lockId, seconds, lockInfo);
        }                        
    }
}
