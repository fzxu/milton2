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
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.common.Service;
import java.util.logging.Level;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.DefaultFtpHandler;
import org.apache.ftpserver.listener.ListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts a milton resource factory into an FTP file system, which allows integration
 * with Apache FTP. ie with this class a milton data source can be accessed by 
 * webdav and FTP simultaneously.
 * 
 * Implements the Service interface from Berry to allow starting or stopping.
 * 
 * The default behaviour is to start the FTP server as part of object construction
 *
 * @author bradm
 */
public class MiltonFtpAdapter implements FileSystemFactory, Service {

    private static final Logger log = LoggerFactory.getLogger( MiltonFtpAdapter.class );
    private final ResourceFactory resourceFactory;
    private final FtpServer server;

    /**
     * Just sets dependencies. Does NOT start the server
     *
     * @param resourceFactory
     * @param server
     */
    public MiltonFtpAdapter( ResourceFactory resourceFactory, FtpServer server ) {
        this.resourceFactory = resourceFactory;
        this.server = server;
    }

    public MiltonFtpAdapter( ResourceFactory wrapped, UserManager userManager ) throws FtpException {
        this( wrapped, userManager, null );
    }

    /**
     * Creates and starts the FTP server on port 21
     *
     * @param wrapped
     * @param userManager
     * @param actionListener
     * @throws FtpException
     */
    public MiltonFtpAdapter( ResourceFactory wrapped, UserManager userManager, FtpActionListener actionListener ) throws FtpException {
        this( wrapped, userManager, actionListener, 21, true );
    }

    /**
     * Creates and starts the FTP server on the given port
     * 
     * @param wrapped
     * @param userManager
     * @param port
     * @throws FtpException
     */
    public MiltonFtpAdapter( ResourceFactory wrapped, UserManager userManager, int port ) throws FtpException {
        this( wrapped, userManager, null, port, true );
    }

    /**
     * Creates and optionally starts the server
     *
     * @param wrapped
     * @param userManager
     * @param actionListener
     * @param port
     * @param autoStart - whether or not to start the server. If false the server can be started with the start() method
     * @throws FtpException
     */
    public MiltonFtpAdapter( ResourceFactory wrapped, UserManager userManager, FtpActionListener actionListener, int port, boolean autoStart ) throws FtpException {
        log.debug( "creating FTP adapter on port: " + port);
        this.resourceFactory = wrapped;
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory factory;
        if( actionListener != null ) {
            log.debug( "using customised milton listener factory" );
            MiltonFtpHandler ftpHandler = new MiltonFtpHandler( new DefaultFtpHandler(), actionListener );
            factory = new MiltonListenerFactory( ftpHandler );
        } else {
            factory = new ListenerFactory();
        }
        factory.setPort( port );
        serverFactory.addListener( "default", factory.createListener() );

        // VERY IMPORTANT
        serverFactory.setFileSystem( this );


        serverFactory.setUserManager( userManager );
        server = serverFactory.createServer();
        if( autoStart ) {            
            start();
        } else {
            log.info("autoStart is false, so not starting FTP server just yet..");
        }
    }

    public void close() {
        if( server != null ) {
            server.stop();
        }
    }

    public Resource getResource( Path path, String host ) throws NotAuthorizedException, BadRequestException {
        return resourceFactory.getResource( host, path.toString() );
    }

    @Override
    public FileSystemView createFileSystemView( User user ) throws FtpException {
        MiltonUser mu = (MiltonUser) user;
        Resource root;
        try {
            root = resourceFactory.getResource( mu.domain, "/" );
        } catch (NotAuthorizedException ex) {
            throw new FtpException(ex);
        } catch (BadRequestException ex) {
            throw new FtpException(ex);
        }
        return new MiltonFsView( Path.root, (CollectionResource) root, resourceFactory, (MiltonUser) user );
    }

    @Override
    public void start() {
        log.debug( "starting the FTP server on port" );
        try {
            server.start();
        } catch( FtpException ex ) {
            throw new RuntimeException( ex );
        }
    }

    @Override
    public void stop() {
        server.stop();
    }
}
