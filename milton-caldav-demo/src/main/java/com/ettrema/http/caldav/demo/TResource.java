package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.bradmcevoy.http.Resource;
import com.ettrema.http.AccessControlledResource;
import com.ettrema.http.acl.Principal;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class TResource extends AbstractResource implements GetableResource, PropFindableResource, DeletableResource, MoveableResource,
    CopyableResource, DigestResource, AccessControlledResource, LockableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResource.class );
    private LockToken currentLock;

    public TResource( TFolderResource parent, String name ) {
        super( parent, name );
    }

    protected abstract Object clone( TFolderResource newParent );

    public String getPrincipalURL() {
        return user;
    }

    public void setSecure( String user, String password ) {
        this.user = user;
        this.password = password;
    }

    public String getHref() {
        if( parent == null ) {
            return "";
        } else {
            String s = parent.getHref();
            if( !s.endsWith( "/" ) ) {
                s = s + "/";
            }
            s = s + name;
            if( this instanceof CollectionResource ) {
                s = s + "/";
            }
            return s;
        }
    }

    public Long getContentLength() {
        return null;
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return (long) 10;
    }

    public void moveTo( CollectionResource rDest, String name ) {
        log.debug( "moving.." );
        TFolderResource d = (TFolderResource) rDest;
        this.parent.children.remove( this );
        this.parent = d;
        this.parent.children.add( this );
        this.name = name;
    }

    public Date getCreateDate() {
        return createdDate;
    }

    public void delete() {
        if( this.parent == null ) {
            throw new RuntimeException( "attempt to delete root" );
        }

        if( this.parent.children == null ) {
            throw new NullPointerException( "children is null" );
        }
        this.parent.children.remove( this );
    }

    public void copyTo( CollectionResource toCollection, String name ) {
        TResource rClone;
        rClone = (TResource) this.clone( (TFolderResource) toCollection );
        rClone.name = name;
    }

    public int compareTo( Resource o ) {
        if( o instanceof TResource ) {
            TResource res = (TResource) o;
            return this.getName().compareTo( res.getName() );
        } else {
            return -1;
        }
    }

    /**
     * This is required for the PropPatchableResource interface, but should
     * not be implemented.
     *
     * Implement CustomPropertyResource or MultiNamespaceCustomPropertyResource instead
     *
     * @param fields
     */
    public void setProperties( Fields fields ) {
    }

    protected void print( PrintWriter printer, String s ) {
        printer.print( s );
    }

    public final LockResult lock( LockTimeout lockTimeout, LockInfo lockInfo ) {
        log.trace( "Lock : " + lockTimeout + " info : " + lockInfo + " on resource : " + getName() + " in : " + parent );
        LockToken token = new LockToken();
        token.info = lockInfo;
        token.timeout = LockTimeout.parseTimeout( "30" );
        token.tokenId = UUID.randomUUID().toString();
        currentLock = token;
        return LockResult.success( token );
    }

    public final LockResult refreshLock( String tokenId ) {
        log.trace( "RefreshLock : " + tokenId + " on resource : " + getName() + " in : " + parent );
        //throw new UnsupportedOperationException("Not supported yet.");
        LockToken token = new LockToken();
        token.info = null;
        token.timeout = LockTimeout.parseTimeout( "30" );
        token.tokenId = currentLock.tokenId;
        currentLock = token;
        return LockResult.success( token );
    }

    public void unlock( String arg0 ) {
        log.trace( "UnLock : " + arg0 + " on resource : " + getName() + " in : " + parent );
        currentLock = null;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public final LockToken getCurrentLock() {
        log.trace( "GetCurrentLock" );
        return currentLock;
    }

    public boolean isDigestAllowed() {
        return true;
    }

    public Map<Principal, List<Priviledge>> getAccessControlList() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Priviledge> getPriviledges(Auth auth) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPriviledges(Principal principal, boolean isGrantOrDeny, List<Priviledge> privs) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
}
