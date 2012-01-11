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


import com.bradmcevoy.property.PropertySource;
import java.net.Socket;
import java.util.List;

/**
 * LDAP server, handle LDAP directory requests.
 */
public class LdapServer extends AbstractServer {
    /**
     * Default LDAP port
     */
    public static final int DEFAULT_PORT = 389;
	
	private final UserFactory userSessionFactory;
	
	private final List<PropertySource> propertySources;

	
    /**
     * Create a ServerSocket to listen for connections.
     * Start the thread.
     *
     * @param port pop listen port, 389 if not defined (0)
     */
    public LdapServer(UserFactory userSessionFactory, List<PropertySource> propertySources, int port, boolean nosslFlag, String bindAddress) {
        super(LdapServer.class.getName(), port, LdapServer.DEFAULT_PORT, bindAddress);
		this.userSessionFactory = userSessionFactory;
		this.propertySources = propertySources;
        this.nosslFlag = nosslFlag;
    }

    @Override
    public String getProtocolName() {
        return "LDAP";
    }

    @Override
    public AbstractConnection createConnectionHandler(Socket clientSocket) {
        return new LdapConnection(clientSocket, userSessionFactory, propertySources);
    }
}