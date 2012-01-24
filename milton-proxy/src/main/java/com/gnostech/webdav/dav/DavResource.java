package com.gnostech.webdav.dav;




import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.http11.auth.DigestGenerator;
import java.text.DateFormat;
import java.util.Date;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.Utils;
import java.util.UUID;
import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import waffle.windows.auth.IWindowsIdentity;

public class DavResource implements PropFindableResource//, DigestResource
{
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( DavResource.class );
    TLock lock;
    private String user = null;

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public boolean authorise( Request request, Method method, Auth auth ) {
        auth = request.getAuthorization();
        if(auth != null){
            user = auth.getUser();
        }else{
            user = "authorise";
        }
        return true;
        /*
        log.debug( "authorise" );
         if( auth == null ) {
             return false;
         } else {
             String user = auth.getUser();
             return true;  // Eg only allow admins
         }
         * 
         */
    }

    @Override
    public String getRealm() {
        return null;
    }
    
    @Override
    public Object authenticate( String user, String requestedPassword ) {
        log.debug( "authenticated " + user  );
        return user;
    }

    public String getAuthUser() {
        return user;
    }

 public LockToken getCurrentLock() {
        if( this.lock == null ) return null;
        LockToken token = new LockToken();
        token.info = this.lock.lockInfo;
        token.timeout = new LockTimeout( this.lock.seconds );
        token.tokenId = this.lock.lockId;
        return token;
    }

    public LockResult lock( LockTimeout timeout, LockInfo lockInfo ) {
	System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  locking");
//        if( lock != null ) {
//            // todo
//            throw new RuntimeException("already locked");
//        }

        LockTimeout.DateAndSeconds lockedUntil = timeout.getLockedUntil( 60l, 3600l );

        this.lock = new TLock( lockedUntil.date, UUID.randomUUID().toString(), lockedUntil.seconds, lockInfo );

        LockToken token = new LockToken();
        token.info = lockInfo;
        token.timeout = new LockTimeout( lockedUntil.seconds );
        token.tokenId = this.lock.lockId;

        return LockResult.success( token );
    }

    public LockResult refreshLock( String token ) {
        if( lock == null ) throw new RuntimeException( "not locked" );
        if( !lock.lockId.equals( token ) )
            throw new RuntimeException( "invalid lock id" );
        this.lock = lock.refresh();
        LockToken tok = makeToken();
        return LockResult.success( tok );
    }

    public void unlock( String tokenId ) {
        if( lock == null ) {
           // log.warn( "request to unlock not locked resource" );
            return;
        }
        if( !lock.lockId.equals( tokenId ) )
            throw new RuntimeException( "Invalid lock token" );
        this.lock = null;
        System.out.println("-----------------------------------------  unlocked");
        
    }

    LockToken makeToken() {
        LockToken token = new LockToken();
        token.info = lock.lockInfo;
        token.timeout = new LockTimeout( lock.seconds );
        token.tokenId = lock.lockId;
        return token;
    }

    public Date getCreateDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getUniqueId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Date getModifiedDate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
class TLock {

        final Date lockedUntil;
        final String lockId;
        final long seconds;
        final LockInfo lockInfo;

        public TLock( Date lockedUntil, String lockId, long seconds, LockInfo lockInfo ) {
            this.lockedUntil = lockedUntil;
            this.lockId = lockId;
            this.seconds = seconds;
            this.lockInfo = lockInfo;
        }

        TLock refresh() {
            Date dt = Utils.addSeconds( Utils.now(), seconds );
            return new TLock( dt, lockId, seconds, lockInfo );
        }
        
    }
         
        
}