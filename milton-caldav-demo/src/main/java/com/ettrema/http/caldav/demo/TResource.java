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
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import com.ettrema.http.AccessControlledResource;
import java.io.PrintWriter;
import java.util.Date;
import java.util.UUID;

public abstract class TResource implements GetableResource, PropFindableResource, DeletableResource, MoveableResource,
    CopyableResource, DigestResource, AccessControlledResource, LockableResource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( TResource.class );
    String name;
    Date modDate;
    Date createdDate;
    TFolderResource parent;
    private String user;
    private String password;
    private LockToken currentLock;

    protected abstract Object clone( TFolderResource newParent );

    public TResource( TFolderResource parent, String name ) {
        this.parent = parent;
        this.name = name;
        modDate = new Date();
        createdDate = new Date();
        if( parent != null ) {
            checkAndRemove( parent, name );
            parent.children.add( this );
        }
    }

    public String getPrincipalURL() {
        return user;
    }



    public void setSecure( String user, String password ) {
        this.user = user;
        this.password = password;
    }

    public String getHref() {
        if( parent == null ) {
            return "/webdav/";
        } else {
            String s = parent.getHref();
            if( !s.endsWith( "/" ) ) s = s + "/";
            s = s + name;
            if( this instanceof CollectionResource ) s = s + "/";
            return s;
        }
    }

    public Long getContentLength() {
        return null;
    }

    public String checkRedirect( Request request ) {
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

    public String getName() {
        return name;
    }

    public Object authenticate( String user, String requestedPassword ) {
        log.debug( "authentication: " + user + " - " + requestedPassword + " = " + password );
        if( this.user == null ) {
            log.debug( "no user defined, so allow access" );
            return true;
        }
        if( !user.equals( this.user ) ) {
            return null;
        }
        if( password == null ) {
            if( requestedPassword == null || requestedPassword.length() == 0 ) {
                return "ok";
            } else {
                return null;
            }
        } else {
            if( password.equals( requestedPassword ) ) {
                return "ok";
            } else {
                return null;
            }
        }
    }

    public Object authenticate( DigestResponse digestRequest ) {
        DigestGenerator dg = new DigestGenerator();
        String serverResponse = dg.generateDigest( digestRequest, password );
        String clientResponse = digestRequest.getResponseDigest();

        log.debug( "server resp: " + serverResponse );
        log.debug( "given response: " + clientResponse );

        if( serverResponse.equals( clientResponse ) ) {
            return "ok";
        } else {
            return null;
        }
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        log.debug( "authorise" );
        if( auth == null ) {
            if( this.user == null ) {
                return true;
            } else {
                return false;
            }
        } else {
            return ( this.user == null || auth.getUser().equals( this.user ) );
        }
    }

    public String getRealm() {
        return "testrealm@host.com";
    }

    public Date getModifiedDate() {
        return modDate;


    }

    public void delete() {
        if( this.parent == null )
            throw new RuntimeException( "attempt to delete root" );

        if( this.parent.children == null )
            throw new NullPointerException( "children is null" );
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

    public String getUniqueId() {
        return this.hashCode() + "";
    }

    private void checkAndRemove( TFolderResource parent, String name ) {
        TResource r = (TResource) parent.child( name );
        if( r != null ) parent.children.remove( r );
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


  public final LockResult lock(LockTimeout lockTimeout, LockInfo lockInfo)
  {
    log.trace("Lock : " + lockTimeout + " info : " + lockInfo + " on resource : " + getName() + " in : " + parent);
    LockToken token = new LockToken();
    token.info = lockInfo;
    token.timeout = LockTimeout.parseTimeout("30");
    token.tokenId = UUID.randomUUID().toString();
    currentLock = token;
    return LockResult.success(token);
  }

  public final LockResult refreshLock(String tokenId)
  {
    log.trace("RefreshLock : " + tokenId + " on resource : " + getName() + " in : " + parent);
    //throw new UnsupportedOperationException("Not supported yet.");
    LockToken token = new LockToken();
    token.info = null;
    token.timeout = LockTimeout.parseTimeout("30");
    token.tokenId = currentLock.tokenId;
    currentLock = token;
    return LockResult.success(token);
  }

  public void unlock(String arg0)
  {
    log.trace("UnLock : " + arg0 + " on resource : " + getName() + " in : " + parent);
    currentLock = null;
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  public final LockToken getCurrentLock()
  {
    log.trace("GetCurrentLock");
    return currentLock;
  }
}
