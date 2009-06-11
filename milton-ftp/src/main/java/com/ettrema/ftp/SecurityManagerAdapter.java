package com.ettrema.ftp;

import java.util.List;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;

import com.bradmcevoy.http.SecurityManager;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Adapts a milton SecurityManager to be an apache FTP UserManager
 * 
 * @author brad
 */
public class SecurityManagerAdapter implements UserManager {

    private static final Logger log = LoggerFactory.getLogger( SecurityManagerAdapter.class );
    SecurityManager securityManager;

    public SecurityManagerAdapter( SecurityManager securityManager ) {
        this.securityManager = securityManager;
    }

    public User getUserByName( String name ) throws FtpException {
        Object o = securityManager.getUserByName( name );
        if( o != null ) {
            return new MiltonUser( o, name );
        } else {
            return null;
        }
    }

    public String[] getAllUserNames() throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void delete( String arg0 ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void save( User arg0 ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean doesExist( String arg0 ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public User authenticate( Authentication authentication ) throws AuthenticationFailedException {
        if( authentication instanceof UsernamePasswordAuthentication ) {
            UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) authentication;
            String user = upa.getUsername();
            String password = upa.getPassword();
            log.debug( "authenticate: " + user );
            Object oUser = securityManager.authenticate( user, password );
            return new MiltonUser( oUser, user );
        } else if( authentication instanceof AnonymousAuthentication ) {
                log.debug( "anonymous, returning empty user" );
                return new MiltonUser( null, null );
            } else {
                log.warn( "unknown authentication type: " + authentication.getClass() );
                return null;
            }
    }

    public String getAdminName() throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean isAdmin( String arg0 ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public class MiltonUser implements User {

        final Object user;
        final String name;

        public MiltonUser( Object user, String name ) {
            this.user = user;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            throw new UnsupportedOperationException( "Not supported yet." );
        }

        public List<Authority> getAuthorities() {
            return null;
        }

        /**
         *
         * @return - the security implementation specific user object returned
         * by authentication
         */
        public Object getUser() {
            return user;
        }


        /**
         * {@inheritDoc}
         */
        public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
            return null;
        }

        public AuthorizationRequest authorize( AuthorizationRequest request ) {
            log.debug( "authorize: " + request.getClass() );
            return request;
        }

        public int getMaxIdleTime() {
            return 3600;
        }

        public boolean getEnabled() {
            return true;
        }

        public String getHomeDirectory() {
            return "/";
        }
    }
}
