/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2009  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.0
 */
package com.ettrema.ldap;


import com.bradmcevoy.http.webdav.WebDavProtocol;
import com.bradmcevoy.property.PropertySource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LDAP server, handle LDAP directory requests.
 */
public class LdapServer extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(LdapServer.class);
	
    /**
     * Default LDAP port
     */
    public static final int DEFAULT_PORT = 389;
	
	private final UserFactory userSessionFactory;	
	private final List<PropertySource> propertySources;
	private final SearchManager searchManager = new SearchManager();
	
    protected boolean nosslFlag;
    private int port;
	private String bindAddress;
	
	private boolean allowRemote;
	private File keystoreFile;
	private String keystoreType;
	private String keystorePass;	
    private ServerSocket serverSocket;	

	
    /**
     * Create a ServerSocket to listen for connections.
     * Start the thread.
     *
     * @param port pop listen port, 389 if not defined (0)
     */
    public LdapServer(UserFactory userSessionFactory, List<PropertySource> propertySources, int port, boolean nosslFlag, String bindAddress) {
        super(LdapServer.class.getName());
        setDaemon(true);
        if (port == 0) {
            this.port = LdapServer.DEFAULT_PORT;
        } else {
            this.port = port;
        }
		this.bindAddress = bindAddress;		
		this.userSessionFactory = userSessionFactory;
		this.propertySources = propertySources;
        this.nosslFlag = nosslFlag;
    }
	
    public LdapServer(UserFactory userSessionFactory, List<PropertySource> propertySources) {
        super(LdapServer.class.getName());
        setDaemon(true);
		this.userSessionFactory = userSessionFactory;
		this.propertySources = propertySources;
    }
	
	/**
	 * This constructor is for convenience. It uses the list of property sources
	 * from the WebDavProtocol object, freeing the developer from the need
	 * to publicly declare property sources when only the built in ones are used.
	 * 
	 * @param userSessionFactory
	 * @param webDavProtocol 
	 */
    public LdapServer(UserFactory userSessionFactory, WebDavProtocol webDavProtocol) {
        super(LdapServer.class.getName());
        setDaemon(true);
		this.userSessionFactory = userSessionFactory;
		this.propertySources = webDavProtocol.getPropertySources();
    }

	public boolean isNosslFlag() {
		return nosslFlag;
	}

	public void setNosslFlag(boolean nosslFlag) {
		this.nosslFlag = nosslFlag;
	}
		
	public String getBindAddress() {
		return bindAddress;
	}

	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	public void setPort(int port) {
		this.port = port;
	}
	

    public String getProtocolName() {
        return "LDAP";
    }

    public LdapConnection createConnectionHandler(Socket clientSocket) {
        return new LdapConnection(clientSocket, userSessionFactory, propertySources, searchManager);
    }

	@Override
	public synchronized void start() {
		try {
			log.info("Created server, binding to address. bind address: " + bindAddress + " port: " + port);
			bind();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		log.info("Starting the LDAP server thread");
		super.start();
	}

	

    /**
     * Bind server socket on defined port.
     *
     * @throws DavMailException unable to create server socket
     */
    public void bind() throws Exception {
        ServerSocketFactory serverSocketFactory;
        if (keystoreFile == null || keystoreFile.length() == 0 || nosslFlag) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        } else {
            FileInputStream keyStoreInputStream = null;
            try {
                keyStoreInputStream = new FileInputStream(keystoreFile);
                // keystore for keys and certificates
                // keystore and private keys should be password protected...
                KeyStore keystore = KeyStore.getInstance(keystoreType);
                keystore.load(keyStoreInputStream, keystorePass.toCharArray());

                // KeyManagerFactory to create key managers
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

                // initialize KMF to work with keystore
                kmf.init(keystore, keystorePass.toCharArray());

                // SSLContext is environment for implementing JSSE...
                // create ServerSocketFactory
                SSLContext sslContext = SSLContext.getInstance("SSLv3");

                // initialize sslContext to work with key managers
                sslContext.init(kmf.getKeyManagers(), null, null);

                // create ServerSocketFactory from sslContext
                serverSocketFactory = sslContext.getServerSocketFactory();
            } catch (IOException ex) {
                throw new Exception(ex);
            } catch (GeneralSecurityException ex) {
                throw new Exception(ex);
            } finally {
                if (keyStoreInputStream != null) {
                    try {
                        keyStoreInputStream.close();
                    } catch (IOException exc) {
                        log.error("exception closing stream", exc);
                    }
                }
            }
        }
        try {
            // create the server socket
            if (bindAddress == null || bindAddress.length() == 0) {
                serverSocket = serverSocketFactory.createServerSocket(port);
            } else {
                serverSocket = serverSocketFactory.createServerSocket(port, 0, Inet4Address.getByName(bindAddress));
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
    }


    /**
     * The body of the server thread.  Loop forever, listening for and
     * accepting connections from clients.  For each connection,
     * create a Connection object to handle communication through the
     * new Socket.
     */
    @Override
    public void run() {
        Socket clientSocket = null;
        LdapConnection connection = null;
        try {
            //noinspection InfiniteLoopStatement
            while (true) {
				System.out.println("Waiting for connection...");
                clientSocket = serverSocket.accept();
				System.out.println("Accepted socket from: " + clientSocket.getRemoteSocketAddress());
                // set default timeout to 5 minutes
                clientSocket.setSoTimeout(300000);
                log.info("CONNECTION_FROM" + clientSocket.getInetAddress() + port);
                // only accept localhost connections for security reasons
                if (allowRemote ||
                        clientSocket.getInetAddress().isLoopbackAddress()) {
                    connection = createConnectionHandler(clientSocket);
                    connection.start();
                } else {
                    clientSocket.close();
                    log.warn("external connection refused");
                }
            }
        } catch (IOException e) {
            // do not warn if exception on socket close (gateway restart)
            if (!serverSocket.isClosed()) {
                log.warn("exception", e);
            }
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                log.warn("exception", e);
            }
            if (connection != null) {
                connection.close();
            }
        }
		System.out.println("LDAP Server has exited");
    }


    /**
     * Close server socket
     */
    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("LOG_EXCEPTION_CLOSING_SERVER_SOCKET", e);
        }
    }
	
    /**
     * Server socket TCP port
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

	public String getKeystorePass() {
		return keystorePass;
	}

	public void setKeystorePass(String keystorePass) {
		this.keystorePass = keystorePass;
	}

	public String getKeystoreType() {
		return keystoreType;
	}

	public void setKeystoreType(String keystoreType) {
		this.keystoreType = keystoreType;
	}

	public File getKeystoreFile() {
		return keystoreFile;
	}

	public void setKeystoreFile(File keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	public boolean isAllowRemote() {
		return allowRemote;
	}

	public void setAllowRemote(boolean allowRemote) {
		this.allowRemote = allowRemote;
	}
		
}