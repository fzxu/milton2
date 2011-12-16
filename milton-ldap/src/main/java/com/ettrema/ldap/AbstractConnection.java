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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.ettrema.ldap;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Generic connection common to pop3 and smtp implementations
 */
public class AbstractConnection extends Thread {

	private static final Logger log = LoggerFactory.getLogger(AbstractConnection.class);
	
    protected enum State {
        INITIAL, LOGIN, USER, PASSWORD, AUTHENTICATED, STARTMAIL, RECIPIENT, MAILDATA
    }

    protected static class LineReaderInputStream extends PushbackInputStream {
        final String encoding;

        /**
         * @inheritDoc
         */
        protected LineReaderInputStream(InputStream in, String encoding) {
            super(in);
            if (encoding == null) {
                this.encoding = "ASCII";
            } else {
                this.encoding = encoding;
            }
        }

        public String readLine() throws IOException {
            ByteArrayOutputStream baos = null;
            int b;
            while ((b = read()) > -1) {
                if (b == '\r') {
                    int next = read();
                    if (next != '\n') {
                        unread(next);
                    }
                    break;
                } else if (b == '\n') {
                    break;
                }
                if (baos == null) {
                    baos = new ByteArrayOutputStream();
                }
                baos.write(b);
            }
            if (baos != null) {
                return new String(baos.toByteArray(), encoding);
            } else {
                return null;
            }
        }

        /**
         * Read byteSize bytes from inputStream, return content as String.
         *
         * @param byteSize content size
         * @return content
         * @throws IOException on error
         */
        public String readContentAsString(int byteSize) throws IOException {
            return new String(readContent(byteSize), encoding);
        }

        /**
         * Read byteSize bytes from inputStream, return content as byte array.
         *
         * @param byteSize content size
         * @return content
         * @throws IOException on error
         */
        public byte[] readContent(int byteSize) throws IOException {
            byte[] buffer = new byte[byteSize];
            int startIndex = 0;
            int count = 0;
            while (count >= 0 && startIndex < byteSize) {
                count = in.read(buffer, startIndex, byteSize - startIndex);
                startIndex += count;
            }
            if (startIndex < byteSize) {
                throw new RuntimeException("EXCEPTION_END_OF_STREAM");
            }

            return buffer;
        }
    }

    protected final Socket client;

    protected LineReaderInputStream in;
    protected OutputStream os;
    // user name and password initialized through connection
    protected String userName;
    protected String password;
    // connection state
    protected State state = State.INITIAL;

    /**
     * Only set the thread name and socket
     *
     * @param name         thread type name
     * @param clientSocket client socket
     */
    public AbstractConnection(String name, Socket clientSocket) {
        super(name + '-' + clientSocket.getPort());
        this.client = clientSocket;
        setDaemon(true);
    }

    /**
     * Initialize the streams and set thread name.
     *
     * @param name         thread type name
     * @param clientSocket client socket
     * @param encoding     socket stream encoding
     */
    public AbstractConnection(String name, Socket clientSocket, String encoding) {
        super(name + '-' + clientSocket.getPort());
        this.client = clientSocket;
        try {
            in = new LineReaderInputStream(client.getInputStream(), encoding);
            os = new BufferedOutputStream(client.getOutputStream());
        } catch (IOException e) {
            close();
            log.error("LOG_EXCEPTION_GETTING_SOCKET_STREAMS", e);
        }
    }

    /**
     * Send message to client followed by CRLF.
     *
     * @param message message
     * @throws IOException on error
     */
    public void sendClient(String message) throws IOException {
        sendClient(null, message);
    }

    /**
     * Send prefix and message to client followed by CRLF.
     *
     * @param prefix  prefix
     * @param message message
     * @throws IOException on error
     */
    public void sendClient(String prefix, String message) throws IOException {
        if (prefix != null) {
            os.write(prefix.getBytes());
            log.debug("LOG_SEND_CLIENT_PREFIX_MESSAGE", prefix, message);
        } else {
            log.debug("LOG_SEND_CLIENT_MESSAGE", message);
        }
        os.write(message.getBytes());
        os.write((char) 13);
        os.write((char) 10);
        os.flush();
    }

    /**
     * Send only bytes to client.
     *
     * @param messageBytes content
     * @throws IOException on error
     */
    public void sendClient(byte[] messageBytes) throws IOException {
        sendClient(messageBytes, 0, messageBytes.length);
    }

    /**
     * Send only bytes to client.
     *
     * @param messageBytes content
     * @param offset       the start offset in the data.
     * @param length       the number of bytes to write.
     * @throws IOException on error
     */
    public void sendClient(byte[] messageBytes, int offset, int length) throws IOException {
        //StringBuffer logBuffer = new StringBuffer("> ");
        //logBuffer.append(new String(messageBytes, offset, length));
        //DavGatewayTray.debug(logBuffer.toString());
        os.write(messageBytes, offset, length);
        os.flush();
    }

    /**
     * Read a line from the client connection.
     * Log message to logger
     *
     * @return command line or null
     * @throws IOException when unable to read line
     */
    public String readClient() throws IOException {
        String line = in.readLine();
        if (line != null) {
            if (line.startsWith("PASS")) {
                log.debug("LOG_READ_CLIENT_PASS");
                // SMTP LOGIN
            } else if (line.startsWith("AUTH LOGIN ")) {
                log.debug("LOG_READ_CLIENT_AUTH_LOGIN");
                // IMAP LOGIN
            } else if (state == State.INITIAL && line.indexOf(' ') >= 0 &&
                    line.substring(line.indexOf(' ') + 1).toUpperCase().startsWith("LOGIN")) {
                log.debug("LOG_READ_CLIENT_LOGIN");
            } else if (state == State.PASSWORD) {
                log.debug("LOG_READ_CLIENT_PASSWORD");
                // HTTP Basic Authentication
            } else if (line.startsWith("Authorization:")) {
                log.debug("LOG_READ_CLIENT_AUTHORIZATION");
            } else if (line.startsWith("AUTH PLAIN")) {
                log.debug("LOG_READ_CLIENT_AUTH_PLAIN");
            } else {
                log.debug("LOG_READ_CLIENT_LINE", line);
            }
        }
        return line;
    }

    /**
     * Close client connection, streams and Exchange session .
     */
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e2) {
                log.warn("LOG_EXCEPTION_CLOSING_CLIENT_INPUT_STREAM", e2);
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e2) {
                log.warn("LOG_EXCEPTION_CLOSING_CLIENT_OUTPUT_STREAM", e2);
            }
        }
        try {
            client.close();
        } catch (IOException e2) {
            log.warn("LOG_EXCEPTION_CLOSING_CLIENT_SOCKET", e2);
        }
    }

    protected String base64Encode(String value) {
        return new String(new Base64().encode(value.getBytes()));
    }

    protected String base64Decode(String value) {
        return new String(new Base64().decode(value.getBytes()));
    }
}
