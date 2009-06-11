////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2009, Suncorp Metway Limited. All rights reserved.
//
// This is unpublished proprietary source code of Suncorp Metway Limited.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
////////////////////////////////////////////////////////////////////////////////
package com.ettrema.ftp;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.UserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author u370681
 */
public class MiltonFtpAdapter implements FileSystemFactory {

    private static final Logger log = LoggerFactory.getLogger( MiltonFtpAdapter.class );
    private FtpServerFactory serverFactory;
    private final ResourceFactory resourceFactory;
    private final FtpServer server;
    private final String host;

    public MiltonFtpAdapter( String host, ResourceFactory wrapped, UserManagerFactory userManagerFactory ) throws FtpException {
        this.host = host;
        this.resourceFactory = wrapped;

        UserManager um = userManagerFactory.createUserManager();

        serverFactory = new FtpServerFactory();
        serverFactory.setFileSystem( this );
        serverFactory.setUserManager( um );
        server = serverFactory.createServer();
        server.start();
    }

    public MiltonFtpAdapter( String host, ResourceFactory wrapped, UserManager userManager ) throws FtpException {
        this.host = host;
        this.resourceFactory = wrapped;

        serverFactory = new FtpServerFactory();
        serverFactory.setFileSystem( this );
        serverFactory.setUserManager( userManager);
        server = serverFactory.createServer();
        server.start();
    }

    /**
     * Creates a user manager with one user, specified with these constructor arguments
     *
     * @param host
     * @param wrapped
     * @param userName
     * @param password
     * @param homeDir
     * @throws org.apache.ftpserver.ftplet.FtpException
     */
    public MiltonFtpAdapter( String host, ResourceFactory wrapped, String userName, String password, String homeDir ) throws FtpException {
        this.host = host;
        this.resourceFactory = wrapped;

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
//        userManagerFactory.setFile(new File("c:\\users.properties"));
        userManagerFactory.setPasswordEncryptor( new SaltedPasswordEncryptor() );
        UserManager um = userManagerFactory.createUserManager();

        BaseUser user = new BaseUser();
        user.setName( userName );
        user.setPassword( password );
        user.setHomeDirectory( homeDir );
        um.save( user );

        serverFactory = new FtpServerFactory();
        serverFactory.setFileSystem( this );
        serverFactory.setUserManager( um );
        server = serverFactory.createServer();
        server.start();
    }

    public Resource getResource( Path path ) {
        return resourceFactory.getResource( host, path.toString() );
    }

    public FileSystemView createFileSystemView( User user ) throws FtpException {
        Resource root = resourceFactory.getResource( host, "/" );
        return new MiltonFsView( host, Path.root, (CollectionResource) root ,resourceFactory, (SecurityManagerAdapter.MiltonUser)user);
    }

}
