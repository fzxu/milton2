package com.ettrema.ftp;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import com.bradmcevoy.http.SecurityManager;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Wraps a UserManager from apache FTP to provide a SecurityManager for
 * milton
 *
 *
 * @author brad
 */
public class UserManagerAdapter implements SecurityManager{

    private static final Logger log = LoggerFactory.getLogger( UserManagerAdapter.class );

    private UserManager userManager;
    private String realm;

    public UserManagerAdapter() {
    }

    public UserManagerAdapter( UserManager userManager, String realm ) {
        this.userManager = userManager;
    }

    public Object authenticate( String userName, String password ) {
        User user;
        try {
            user = this.userManager.getUserByName( userName );
        } catch( FtpException ex ) {
            log.warn( "exception loading user: " + userName, ex);
            return null;
        }
        if( user == null ) {
            return null;
        } else {
            String actual = user.getPassword();
            if( actual == null ) {
                return password == null || password.length() == 0;
            } else {
                if( actual.equals( password)) {
                    return user;
                } else {
                    return null;
                }
            }
        }
    }

    public boolean authorise( Request request, Method method, Auth auth, Resource resource ) {
        return (auth.getTag() instanceof User);
    }

    public String getRealm() {
        return realm;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager( UserManager userManager ) {
        this.userManager = userManager;
    }

    public void setRealm(String s) {
        this.realm = s;
    }
}
