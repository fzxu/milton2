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

import com.ettrema.common.LogUtils;
import com.sun.jndi.ldap.Ber;
import com.sun.jndi.ldap.BerDecoder;
import com.sun.jndi.ldap.BerEncoder;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import javax.security.auth.callback.*;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle a caldav connection.
 * 
 * This is the server part of a LDAP client to server connection. This will
 * locate information in some user repository (such as a milton carddav implementation)
 * and format the results as LDAP messages.
 * 
 */
public class LdapConnection extends AbstractConnection {

	private static final Logger log = LoggerFactory.getLogger(LdapConnection.class);
	/**
	 * Davmail base context
	 */
	static final String BASE_CONTEXT = "ou=people";
	/**
	 * OSX server (OpenDirectory) base context
	 */
	static final String OD_BASE_CONTEXT = "o=od";
	static final String OD_USER_CONTEXT = "cn=users, o=od";
	static final String OD_CONFIG_CONTEXT = "cn=config, o=od";
	static final String COMPUTER_CONTEXT = "cn=computers, o=od";
	static final String OD_GROUP_CONTEXT = "cn=groups, o=od";
	// TODO: adjust Directory Utility settings
	static final String COMPUTER_CONTEXT_LION = "cn=computers,o=od";
	static final String OD_USER_CONTEXT_LION = "cn=users, ou=people";
	/**
	 * Root DSE naming contexts (default and OpenDirectory)
	 */
	static final List<String> NAMING_CONTEXTS = new ArrayList<String>();

	static {
		NAMING_CONTEXTS.add(BASE_CONTEXT);
		NAMING_CONTEXTS.add(OD_BASE_CONTEXT);
	}
	static final List<String> PERSON_OBJECT_CLASSES = new ArrayList<String>();

	static {
		PERSON_OBJECT_CLASSES.add("top");
		PERSON_OBJECT_CLASSES.add("person");
		PERSON_OBJECT_CLASSES.add("organizationalPerson");
		PERSON_OBJECT_CLASSES.add("inetOrgPerson");
		// OpenDirectory class for iCal
		PERSON_OBJECT_CLASSES.add("apple-user");
	}

	/**
	 * OSX constant computer guid (used by iCal attendee completion)
	 */
	static final String COMPUTER_GUID = "52486C30-F0AB-48E3-9C37-37E9B28CDD7B";
	/**
	 * OSX constant virtual host guid (used by iCal attendee completion)
	 */
	static final String VIRTUALHOST_GUID = "D6DD8A10-1098-11DE-8C30-0800200C9A66";
	/**
	 * OSX constant value for attribute apple-serviceslocator
	 */
	static final HashMap<String, String> STATIC_ATTRIBUTE_MAP = new HashMap<String, String>();

	static {
		STATIC_ATTRIBUTE_MAP.put("apple-serviceslocator", COMPUTER_GUID + ':' + VIRTUALHOST_GUID + ":calendar");
	}

	// LDAP version
	// static final int LDAP_VERSION2 = 0x02;
	static final int LDAP_VERSION3 = 0x03;
	// LDAP request operations
	static final int LDAP_REQ_BIND = 0x60;
	static final int LDAP_REQ_SEARCH = 0x63;
	static final int LDAP_REQ_UNBIND = 0x42;
	static final int LDAP_REQ_ABANDON = 0x50;
	// LDAP response operations
	static final int LDAP_REP_BIND = 0x61;
	static final int LDAP_REP_SEARCH = 0x64;
	static final int LDAP_REP_RESULT = 0x65;
	static final int LDAP_SASL_BIND_IN_PROGRESS = 0x0E;
	// LDAP return codes
	static final int LDAP_OTHER = 80;
	static final int LDAP_SUCCESS = 0;
	static final int LDAP_SIZE_LIMIT_EXCEEDED = 4;
	static final int LDAP_INVALID_CREDENTIALS = 49;
	// LDAP filter code
	static final int LDAP_FILTER_AND = 0xa0;
	static final int LDAP_FILTER_OR = 0xa1;
	static final int LDAP_FILTER_NOT = 0xa2;
	// LDAP filter operators
	static final int LDAP_FILTER_SUBSTRINGS = 0xa4;
	//static final int LDAP_FILTER_GE = 0xa5;
	//static final int LDAP_FILTER_LE = 0xa6;
	static final int LDAP_FILTER_PRESENT = 0x87;
	//static final int LDAP_FILTER_APPROX = 0xa8;
	static final int LDAP_FILTER_EQUALITY = 0xa3;
	// LDAP filter mode
	static final int LDAP_SUBSTRING_INITIAL = 0x80;
	static final int LDAP_SUBSTRING_ANY = 0x81;
	static final int LDAP_SUBSTRING_FINAL = 0x82;
	// BER data types
	static final int LBER_ENUMERATED = 0x0a;
	static final int LBER_SET = 0x31;
	static final int LBER_SEQUENCE = 0x30;
	// LDAP search scope
	static final int SCOPE_BASE_OBJECT = 0;
	//static final int SCOPE_ONE_LEVEL = 1;
	//static final int SCOPE_SUBTREE = 2;
	/**
	 * For some unknown reason parseIntWithTag is private !
	 */
	static final Method PARSE_INT_WITH_TAG_METHOD;

	static {
		try {
			PARSE_INT_WITH_TAG_METHOD = BerDecoder.class.getDeclaredMethod("parseIntWithTag", int.class);
			PARSE_INT_WITH_TAG_METHOD.setAccessible(true);
		} catch (NoSuchMethodException e) {
			log.error("LOG_UNABLE_TO_GET_PARSEINTWITHTAG", e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * Sasl server for DIGEST-MD5 authentication
	 */
	protected SaslServer saslServer;
	/**
	 * raw connection inputStream
	 */
	protected BufferedInputStream is;
	/**
	 * reusable BER encoder
	 */
	protected final BerEncoder responseBer = new BerEncoder();
	/**
	 * Current LDAP version (used for String encoding)
	 */
	int ldapVersion = LDAP_VERSION3;
	/**
	 * Search threads map
	 */
	final HashMap<Integer, SearchRunnable> searchThreadMap = new HashMap<Integer, SearchRunnable>();
	private final UserFactory userFactory;
	private User user;
	private String currentHostName;	

	/**
	 * Initialize the streams and start the thread.
	 *
	 * @param clientSocket LDAP client socket
	 */
	public LdapConnection(Socket clientSocket, UserFactory userSessionFactory) {
		super(LdapConnection.class.getSimpleName(), clientSocket);
		this.userFactory = userSessionFactory;
		try {
			is = new BufferedInputStream(client.getInputStream());
			os = new BufferedOutputStream(client.getOutputStream());
		} catch (IOException e) {
			close();
			log.error("error", e);
		}
		System.out.println("Created LDAP Connection handler");
	}

	protected boolean isLdapV3() {
		return ldapVersion == LDAP_VERSION3;
	}

	@Override
	public void run() {
		byte[] inbuf = new byte[2048];   // Buffer for reading incoming bytes
		int bytesread;  // Number of bytes in inbuf
		int bytesleft;  // Number of bytes that need to read for completing resp
		int br;         // Temp; number of bytes read from stream
		int offset;     // Offset of where to store bytes in inbuf
		boolean eos;    // End of stream

		try {
			while (true) {
				offset = 0;

				// check that it is the beginning of a sequence
				bytesread = is.read(inbuf, offset, 1);
				if (bytesread < 0) {
					break; // EOF
				}
				System.out.println("read bytes: " + bytesread);

				if (inbuf[offset++] != (Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR)) {
					continue;
				}

				// get length of sequence
				bytesread = is.read(inbuf, offset, 1);
				if (bytesread < 0) {
					break; // EOF
				}
				int seqlen = inbuf[offset++]; // Length of ASN sequence

				// if high bit is on, length is encoded in the
				// subsequent length bytes and the number of length bytes
				// is equal to & 0x80 (i.e. length byte with high bit off).
				if ((seqlen & 0x80) == 0x80) {
					int seqlenlen = seqlen & 0x7f;  // number of length bytes

					bytesread = 0;
					eos = false;

					// Read all length bytes
					while (bytesread < seqlenlen) {
						br = is.read(inbuf, offset + bytesread,
								seqlenlen - bytesread);
						if (br < 0) {
							eos = true;
							break; // EOF
						}
						bytesread += br;
					}

					// end-of-stream reached before length bytes are read
					if (eos) {
						break;  // EOF
					}

					// Add contents of length bytes to determine length
					seqlen = 0;
					for (int i = 0; i < seqlenlen; i++) {
						seqlen = (seqlen << 8) + (inbuf[offset + i] & 0xff);
					}
					offset += bytesread;
				}

				// read in seqlen bytes
				bytesleft = seqlen;
				if ((offset + bytesleft) > inbuf.length) {
					byte[] nbuf = new byte[offset + bytesleft];
					System.arraycopy(inbuf, 0, nbuf, 0, offset);
					inbuf = nbuf;
				}
				while (bytesleft > 0) {
					bytesread = is.read(inbuf, offset, bytesleft);
					if (bytesread < 0) {
						break; // EOF
					}
					offset += bytesread;
					bytesleft -= bytesread;
				}


				handleRequest(inbuf, offset);
			}

		} catch (SocketException e) {
			log.debug("LOG_CONNECTION_CLOSED");
		} catch (SocketTimeoutException e) {
			log.debug("LOG_CLOSE_CONNECTION_ON_TIMEOUT");
		} catch (Exception e) {
			log.error("err", e);
			try {
				sendErr(0, LDAP_REP_BIND, e);
			} catch (IOException e2) {
				log.warn("LOG_EXCEPTION_SENDING_ERROR_TO_CLIENT", e2);
			}
		} finally {
			// cancel all search threads
			synchronized (searchThreadMap) {
				for (SearchRunnable searchRunnable : searchThreadMap.values()) {
					searchRunnable.abandon();
				}
			}
			close();
		}
	}
	protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	protected void handleRequest(byte[] inbuf, int offset) throws IOException {
		//dumpBer(inbuf, offset);
		BerDecoder reqBer = new BerDecoder(inbuf, 0, offset);
		int currentMessageId = 0;
		try {
			reqBer.parseSeq(null);
			currentMessageId = reqBer.parseInt();
			int requestOperation = reqBer.peekByte();

			if (requestOperation == LDAP_REQ_BIND) {
				reqBer.parseSeq(null);
				ldapVersion = reqBer.parseInt();
				userName = reqBer.parseString(isLdapV3());
				if (reqBer.peekByte() == (Ber.ASN_CONTEXT | Ber.ASN_CONSTRUCTOR | 3)) {
					// SASL authentication
					reqBer.parseSeq(null);
					// Get mechanism, usually DIGEST-MD5
					String mechanism = reqBer.parseString(isLdapV3());

					byte[] serverResponse;
					CallbackHandler callbackHandler = new CallbackHandler() {

						@Override
						public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
							// look for username in callbacks
							for (Callback callback : callbacks) {
								if (callback instanceof NameCallback) {
									userName = ((NameCallback) callback).getDefaultName();
									// get password from session pool
									password = userFactory.getUserPassword(userName);
								}
							}
							// handle other callbacks
							for (Callback callback : callbacks) {
								if (callback instanceof AuthorizeCallback) {
									((AuthorizeCallback) callback).setAuthorized(true);
								} else if (callback instanceof PasswordCallback) {
									if (password != null) {
										((PasswordCallback) callback).setPassword(password.toCharArray());
									}
								}
							}
						}
					};
					int status;
					if (reqBer.bytesLeft() > 0 && saslServer != null) {
						byte[] clientResponse = reqBer.parseOctetString(Ber.ASN_OCTET_STR, null);
						serverResponse = saslServer.evaluateResponse(clientResponse);
						status = LDAP_SUCCESS;

						LogUtils.debug(log, "LOG_LDAP_REQ_BIND_USER", currentMessageId, userName);
						user = userFactory.getUser(userName, password);
						LogUtils.debug(log, "LOG_LDAP_REQ_BIND_SUCCESS");

					} else {
						Map<String, String> properties = new HashMap<String, String>();
						properties.put("javax.security.sasl.qop", "auth,auth-int");
						saslServer = Sasl.createSaslServer(mechanism, "ldap", client.getLocalAddress().getHostAddress(), properties, callbackHandler);
						serverResponse = saslServer.evaluateResponse(EMPTY_BYTE_ARRAY);
						status = LDAP_SASL_BIND_IN_PROGRESS;
					}

					responseBer.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
					responseBer.encodeInt(currentMessageId);
					responseBer.beginSeq(LDAP_REP_BIND);
					responseBer.encodeInt(status, LBER_ENUMERATED);
					// server credentials
					responseBer.encodeString("", isLdapV3());
					responseBer.encodeString("", isLdapV3());
					// challenge or response
					if (serverResponse != null) {
						responseBer.encodeOctetString(serverResponse, 0x87);
					}
					responseBer.endSeq();
					responseBer.endSeq();
					sendResponse();

				} else {
					password = reqBer.parseStringWithTag(Ber.ASN_CONTEXT, isLdapV3(), null);

					if (userName.length() > 0 && password.length() > 0) {
						log.debug("LOG_LDAP_REQ_BIND_USER", currentMessageId, userName);
						try {
							user = userFactory.getUser(userName, password);
							LogUtils.debug(log, "LOG_LDAP_REQ_BIND_SUCCESS");
							sendClient(currentMessageId, LDAP_REP_BIND, LDAP_SUCCESS, "");
						} catch (IOException e) {
							LogUtils.debug(log, "LOG_LDAP_REQ_BIND_INVALID_CREDENTIALS");
							sendClient(currentMessageId, LDAP_REP_BIND, LDAP_INVALID_CREDENTIALS, "");
						}
					} else {
						LogUtils.debug(log, "LOG_LDAP_REQ_BIND_ANONYMOUS", currentMessageId);
						// anonymous bind
						sendClient(currentMessageId, LDAP_REP_BIND, LDAP_SUCCESS, "");
					}
				}

			} else if (requestOperation == LDAP_REQ_UNBIND) {
				log.debug("LOG_LDAP_REQ_UNBIND", currentMessageId);
				if (user != null) {
					user = null;
				}
			} else if (requestOperation == LDAP_REQ_SEARCH) {
				reqBer.parseSeq(null);
				String dn = reqBer.parseString(isLdapV3());
				int scope = reqBer.parseEnumeration();
				/*int derefAliases =*/
				reqBer.parseEnumeration();
				int sizeLimit = reqBer.parseInt();
				if (sizeLimit > 100 || sizeLimit == 0) {
					sizeLimit = 100;
				}
				int timelimit = reqBer.parseInt();
				/*boolean typesOnly =*/
				reqBer.parseBoolean();
				LdapFilter ldapFilter = parseFilter(reqBer);
				Set<String> returningAttributes = parseReturningAttributes(reqBer);
				SearchRunnable searchRunnable = new SearchRunnable(userFactory, currentMessageId, dn, scope, sizeLimit, timelimit, ldapFilter, returningAttributes, this); 
				if (BASE_CONTEXT.equalsIgnoreCase(dn) || OD_USER_CONTEXT.equalsIgnoreCase(dn) || OD_USER_CONTEXT_LION.equalsIgnoreCase(dn)) {
					// launch search in a separate thread
					synchronized (searchThreadMap) {
						searchThreadMap.put(currentMessageId, searchRunnable);
					}
					Thread searchThread = new Thread(searchRunnable);
					searchThread.setName(getName() + "-Search-" + currentMessageId);
					searchThread.start();
				} else {
					// no need to create a separate thread, just run
					searchRunnable.run();
				}

			} else if (requestOperation == LDAP_REQ_ABANDON) {
				int abandonMessageId = 0;
				try {
					abandonMessageId = (Integer) PARSE_INT_WITH_TAG_METHOD.invoke(reqBer, LDAP_REQ_ABANDON);
					synchronized (searchThreadMap) {
						SearchRunnable searchRunnable = searchThreadMap.get(abandonMessageId);
						if (searchRunnable != null) {
							searchRunnable.abandon();
							searchThreadMap.remove(currentMessageId);
						}
					}
				} catch (IllegalAccessException e) {
					log.error("", e);
				} catch (InvocationTargetException e) {
					log.error("", e);
				}
				LogUtils.debug(log, "LOG_LDAP_REQ_ABANDON_SEARCH", currentMessageId, abandonMessageId);
			} else {
				LogUtils.debug(log, "LOG_LDAP_UNSUPPORTED_OPERATION", requestOperation);
				sendClient(currentMessageId, LDAP_REP_RESULT, LDAP_OTHER, "Unsupported operation");
			}
		} catch (IOException e) {
			dumpBer(inbuf, offset);
			try {
				sendErr(currentMessageId, LDAP_REP_RESULT, e);
			} catch (IOException e2) {
				log.debug("LOG_EXCEPTION_SENDING_ERROR_TO_CLIENT", e2);
			}
			throw e;
		}
	}

	protected void dumpBer(byte[] inbuf, int offset) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Ber.dumpBER(baos, "LDAP request buffer\n", inbuf, 0, offset);
		try {
			log.debug(new String(baos.toByteArray(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// should not happen
			log.error("", e);
		}
	}

	protected LdapFilter parseFilter(BerDecoder reqBer) throws IOException {
		LdapFilter ldapFilter;
		if (reqBer.peekByte() == LDAP_FILTER_PRESENT) {
			String attributeName = reqBer.parseStringWithTag(LDAP_FILTER_PRESENT, isLdapV3(), null).toLowerCase();
			ldapFilter = new SimpleLdapFilter(userFactory, attributeName);
		} else {
			int[] seqSize = new int[1];
			int ldapFilterType = reqBer.parseSeq(seqSize);
			int end = reqBer.getParsePosition() + seqSize[0];

			ldapFilter = parseNestedFilter(reqBer, ldapFilterType, end);
		}

		return ldapFilter;
	}

	protected LdapFilter parseNestedFilter(BerDecoder reqBer, int ldapFilterType, int end) throws IOException {
		LdapFilter nestedFilter;

		if ((ldapFilterType == LDAP_FILTER_OR) || (ldapFilterType == LDAP_FILTER_AND)
				|| ldapFilterType == LDAP_FILTER_NOT) {
			nestedFilter = new CompoundLdapFilter(ldapFilterType);

			while (reqBer.getParsePosition() < end && reqBer.bytesLeft() > 0) {
				if (reqBer.peekByte() == LDAP_FILTER_PRESENT) {
					String attributeName = reqBer.parseStringWithTag(LDAP_FILTER_PRESENT, isLdapV3(), null).toLowerCase();
					nestedFilter.add(new SimpleLdapFilter(userFactory, attributeName));
				} else {
					int[] seqSize = new int[1];
					int ldapFilterOperator = reqBer.parseSeq(seqSize);
					int subEnd = reqBer.getParsePosition() + seqSize[0];
					nestedFilter.add(parseNestedFilter(reqBer, ldapFilterOperator, subEnd));
				}
			}
		} else {
			// simple filter
			nestedFilter = parseSimpleFilter(reqBer, ldapFilterType);
		}

		return nestedFilter;
	}

	protected LdapFilter parseSimpleFilter(BerDecoder reqBer, int ldapFilterOperator) throws IOException {
		String attributeName = reqBer.parseString(isLdapV3()).toLowerCase();
		int ldapFilterMode = 0;

		StringBuilder value = new StringBuilder();
		if (ldapFilterOperator == LDAP_FILTER_SUBSTRINGS) {
			// Thunderbird sends values with space as separate strings, rebuild value
			int[] seqSize = new int[1];
			/*LBER_SEQUENCE*/
			reqBer.parseSeq(seqSize);
			int end = reqBer.getParsePosition() + seqSize[0];
			while (reqBer.getParsePosition() < end && reqBer.bytesLeft() > 0) {
				ldapFilterMode = reqBer.peekByte();
				if (value.length() > 0) {
					value.append(' ');
				}
				value.append(reqBer.parseStringWithTag(ldapFilterMode, isLdapV3(), null));
			}
		} else if (ldapFilterOperator == LDAP_FILTER_EQUALITY) {
			value.append(reqBer.parseString(isLdapV3()));
		} else {
			log.warn("LOG_LDAP_UNSUPPORTED_FILTER_VALUE");
		}

		String sValue = value.toString();

		if ("uid".equalsIgnoreCase(attributeName) && sValue.equals(userName)) {
			// replace with actual alias instead of login name search, only in Dav mode
			if (sValue.equals(userName)) {
				sValue = user.getAlias();
				LogUtils.debug(log, "LOG_LDAP_REPLACED_UID_FILTER", userName, sValue);
			}
		}

		return new SimpleLdapFilter(userFactory, attributeName, sValue, ldapFilterOperator, ldapFilterMode);
	}

	protected Set<String> parseReturningAttributes(BerDecoder reqBer) throws IOException {
		Set<String> returningAttributes = new HashSet<String>();
		int[] seqSize = new int[1];
		reqBer.parseSeq(seqSize);
		int end = reqBer.getParsePosition() + seqSize[0];
		while (reqBer.getParsePosition() < end && reqBer.bytesLeft() > 0) {
			returningAttributes.add(reqBer.parseString(isLdapV3()).toLowerCase());
		}
		return returningAttributes;
	}

	/**
	 * Send Root DSE
	 *
	 * @param currentMessageId current message id
	 * @throws IOException on error
	 */
	protected void sendRootDSE(int currentMessageId) throws IOException {
		log.debug("LOG_LDAP_SEND_ROOT_DSE");

        Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("objectClass", "top");
		attributes.put("namingContexts", NAMING_CONTEXTS);
		//attributes.put("supportedsaslmechanisms", "PLAIN");

		sendEntry(currentMessageId, "Root DSE", attributes);
	}

	protected void addIf(Map<String, Object> attributes, Set<String> returningAttributes, String name, Object value) {
		if ((returningAttributes.isEmpty()) || returningAttributes.contains(name)) {
			attributes.put(name, value);
		}
	}

	protected String getCurrentHostName() throws UnknownHostException {
		if (currentHostName == null) {
			if (client.getInetAddress().isLoopbackAddress()) {
				// local address, probably using localhost in iCal URL
				currentHostName = "localhost";
			} else {
				// remote address, send fully qualified domain name
				currentHostName = InetAddress.getLocalHost().getCanonicalHostName();
			}
		}
		return currentHostName;
	}
	/**
	 * Cache serviceInfo string value
	 */
	protected String serviceInfo;

	protected String getServiceInfo() {
		if (serviceInfo == null) {
			StringBuilder buffer = new StringBuilder();
			buffer.append("<?xml version='1.0' encoding='UTF-8'?>"
					+ "<!DOCTYPE plist PUBLIC '-//Apple//DTD PLIST 1.0//EN' 'http://www.apple.com/DTDs/PropertyList-1.0.dtd'>"
					+ "<plist version='1.0'>"
					+ "<dict>"
					+ "<key>com.apple.macosxserver.host</key>"
					+ "<array>"
					+ "<string>localhost</string>" + // NOTE: Will be replaced by real hostname
					"</array>"
					+ "<key>com.apple.macosxserver.virtualhosts</key>"
					+ "<dict>"
					+ "<key>" + VIRTUALHOST_GUID + "</key>"
					+ "<dict>"
					+ "<key>hostDetails</key>"
					+ "<dict>"
					+ "<key>http</key>"
					+ "<dict>"
					+ "<key>enabled</key>"
					+ "<true/>"
					+ "</dict>"
					+ "<key>https</key>"
					+ "<dict>"
					+ "<key>disabled</key>"
					+ "<false/>"
					+ "<key>port</key>"
					+ "<integer>0</integer>"
					+ "</dict>"
					+ "</dict>"
					+ "<key>hostname</key>"
					+ "<string>");
			try {
				buffer.append(getCurrentHostName());
			} catch (UnknownHostException ex) {
				buffer.append("Unknown host");
			}
			buffer.append("</string>"
					+ "<key>serviceInfo</key>"
					+ "<dict>"
					+ "<key>calendar</key>"
					+ "<dict>"
					+ "<key>enabled</key>"
					+ "<true/>"
					+ "<key>templates</key>"
					+ "<dict>"
					+ "<key>calendarUserAddresses</key>"
					+ "<array>"
					+ "<string>%(principaluri)s</string>"
					+ "<string>mailto:%(email)s</string>"
					+ "<string>urn:uuid:%(guid)s</string>"
					+ "</array>"
					+ "<key>principalPath</key>"
					+ "<string>/principals/__uuids__/%(guid)s/</string>"
					+ "</dict>"
					+ "</dict>"
					+ "</dict>"
					+ "<key>serviceType</key>"
					+ "<array>"
					+ "<string>calendar</string>"
					+ "</array>"
					+ "</dict>"
					+ "</dict>"
					+ "</dict>"
					+ "</plist>");
			serviceInfo = buffer.toString();
		}
		return serviceInfo;
	}

	/**
	 * Send ComputerContext
	 *
	 * @param currentMessageId    current message id
	 * @param returningAttributes attributes to return
	 * @throws IOException on error
	 */
	protected void sendComputerContext(int currentMessageId, Set<String> returningAttributes) throws IOException {
		List<String> objectClasses = new ArrayList<String>();
		objectClasses.add("top");
		objectClasses.add("apple-computer");
		Map<String, Object> attributes = new HashMap<String, Object>();
		addIf(attributes, returningAttributes, "objectClass", objectClasses);
		addIf(attributes, returningAttributes, "apple-generateduid", COMPUTER_GUID);
		addIf(attributes, returningAttributes, "apple-serviceinfo", getServiceInfo());
		// TODO: remove ?
		addIf(attributes, returningAttributes, "apple-xmlplist", getServiceInfo());
		addIf(attributes, returningAttributes, "apple-serviceslocator", "::anyService");
		addIf(attributes, returningAttributes, "cn", getCurrentHostName());

		String dn = "cn=" + getCurrentHostName() + ", " + COMPUTER_CONTEXT;
		log.debug("LOG_LDAP_SEND_COMPUTER_CONTEXT", dn, attributes);

		sendEntry(currentMessageId, dn, attributes);
	}

	/**
	 * Send Base Context
	 *
	 * @param currentMessageId current message id
	 * @throws IOException on error
	 */
	protected void sendBaseContext(int currentMessageId) throws IOException {
		List<String> objectClasses = new ArrayList<String>();
		objectClasses.add("top");
		objectClasses.add("organizationalUnit");
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("objectClass", objectClasses);
		attributes.put("description", "Milton LDAP Gateway");
		sendEntry(currentMessageId, BASE_CONTEXT, attributes);
	}

	protected void sendEntry(int currentMessageId, String dn, Map<String, Object> attributes) throws IOException {
		LogUtils.trace(log, "sendEntry", currentMessageId, dn, attributes.size());
		// synchronize on responseBer
		synchronized (responseBer) {
			responseBer.reset();
			responseBer.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
			responseBer.encodeInt(currentMessageId);
			responseBer.beginSeq(LDAP_REP_SEARCH);
			responseBer.encodeString(dn, isLdapV3());
			responseBer.beginSeq(LBER_SEQUENCE);
			for (Map.Entry<String, Object> entry : attributes.entrySet()) {
				responseBer.beginSeq(LBER_SEQUENCE);
				responseBer.encodeString(entry.getKey(), isLdapV3());
				responseBer.beginSeq(LBER_SET);
				Object values = entry.getValue();
				if (values instanceof String) {
					responseBer.encodeString((String) values, isLdapV3());
				} else if (values instanceof List) {
					for (Object value : (List) values) {
						responseBer.encodeString((String) value, isLdapV3());
					}
				} else {
					throw new RuntimeException("EXCEPTION_UNSUPPORTED_VALUE: " + values);
				}
				responseBer.endSeq();
				responseBer.endSeq();
			}
			responseBer.endSeq();
			responseBer.endSeq();
			responseBer.endSeq();
			sendResponse();
		}
	}

	protected void sendErr(int currentMessageId, int responseOperation, Exception e) throws IOException {
		String message = e.getMessage();
		if (message == null) {
			message = e.toString();
		}
		sendClient(currentMessageId, responseOperation, LDAP_OTHER, message);
	}

	protected void sendClient(int currentMessageId, int responseOperation, int status, String message) throws IOException {
		responseBer.reset();

		responseBer.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
		responseBer.encodeInt(currentMessageId);
		responseBer.beginSeq(responseOperation);
		responseBer.encodeInt(status, LBER_ENUMERATED);
		// dn
		responseBer.encodeString("", isLdapV3());
		// error message
		responseBer.encodeString(message, isLdapV3());
		responseBer.endSeq();
		responseBer.endSeq();
		sendResponse();
	}

	protected void sendResponse() throws IOException {
		//Ber.dumpBER(System.out, ">\n", responseBer.getBuf(), 0, responseBer.getDataLen());
		os.write(responseBer.getBuf(), 0, responseBer.getDataLen());
		os.flush();
	}

	public User getUser() {
		return user;
	}

	public String getUserName() {
		return userName;
	}

	public UserFactory getUserSessionFactory() {
		return userFactory;
	}
	
	

	public static interface LdapFilter {

		Condition getContactSearchFilter();

		Map<String, Contact> findInGAL(User user, Set<String> returningAttributes, int sizeLimit) throws IOException;

		void add(LdapFilter filter);

		boolean isFullSearch();

		boolean isMatch(Map<String, String> person);
	}
}
