package com.ettrema.ftp;

import java.util.ArrayList;
import java.util.List;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import com.bradmcevoy.http.SecurityManager;

/**
 *
 * TODO
 *
 *
 * @author brad
 */
public class UserManagerAdapter implements UserManager{
    SecurityManager securityManager;

    public UserManagerAdapter( SecurityManager securityManager ) {
        this.securityManager = securityManager;
    }

    public User getUserByName( String name ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String[] getAllUserNames() throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void delete( String name ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public void save( User user ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean doesExist( String name ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public User authenticate( Authentication auth ) throws AuthenticationFailedException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public String getAdminName() throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    public boolean isAdmin( String name ) throws FtpException {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    class MiltonUser implements User {

        final String name;

        public MiltonUser( String name ) {
            this.name = name;
        }
                
        public String getName() {
            return name;
        }

        public String getPassword() {
            return "";
        }

        public List<Authority> getAuthorities() {
            return new ArrayList<Authority>();
        }

        public List<Authority> getAuthorities( Class<? extends Authority> arg0 ) {
            return new ArrayList<Authority>();
        }

        public AuthorizationRequest authorize( AuthorizationRequest req ) {
            return null; // WTF???
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
