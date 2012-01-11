package com.ettrema.ldap;

import com.ettrema.common.LogUtils;
import java.io.IOException;
import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class SearchRunnable implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(SearchRunnable.class);
	
	private final UserFactory userFactory;
	private final int currentMessageId;
	private final String dn;
	private final int scope;
	private final int sizeLimit;
	private final int timelimit;
	private final LdapConnection.LdapFilter ldapFilter;
	private final Set<String> returningAttributes;
	private boolean abandon;
	private final LdapConnection ldapConnection;

	protected SearchRunnable(UserFactory userFactory, int currentMessageId, String dn, int scope, int sizeLimit, int timelimit, LdapConnection.LdapFilter ldapFilter, Set<String> returningAttributes, final LdapConnection ldapConnection) {
		this.userFactory = userFactory;
		this.ldapConnection = ldapConnection;
		this.currentMessageId = currentMessageId;
		this.dn = dn;
		this.scope = scope;
		this.sizeLimit = sizeLimit;
		this.timelimit = timelimit;
		this.ldapFilter = ldapFilter;
		this.returningAttributes = returningAttributes;
	}

	/**
	 * Abandon search.
	 */
	/**
	 * Abandon search.
	 */
	protected void abandon() {
		abandon = true;
	}

	@Override
	public void run() {
		try {
			int size = 0;
			LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH", currentMessageId, dn, scope, sizeLimit, timelimit, ldapFilter.toString(), returningAttributes);
			if (scope == LdapConnection.SCOPE_BASE_OBJECT) {
				if ("".equals(dn)) {
					size = 1;
					ldapConnection.sendRootDSE(currentMessageId);
				} else if (LdapConnection.BASE_CONTEXT.equals(dn)) {
					size = 1;
					// root
					// root
					ldapConnection.sendBaseContext(currentMessageId);
				} else if (dn.startsWith("uid=") && dn.indexOf(',') > 0) {
					if (ldapConnection.getUser() != null) {
						// single user request
						// single user request
						String uid = dn.substring("uid=".length(), dn.indexOf(','));
						Map<String, Contact> persons = null;
						// first search in contact
						// first search in contact
						try {
							// check if this is a contact uid
							Integer.parseInt(uid);
							persons = contactFind(Conditions.isEqualTo("imapUid", uid), returningAttributes, sizeLimit);
						} catch (NumberFormatException e) {							
							// ignore, this is not a contact uid
						}
						// then in GAL
						if (persons == null || persons.isEmpty()) {
							persons = userFactory.galFind(Conditions.isEqualTo("imapUid", uid), LdapUtils.convertLdapToContactReturningAttributes(returningAttributes), sizeLimit);
							Contact person = persons.get(uid.toLowerCase());
							// filter out non exact results
							// filter out non exact results
							if (persons.size() > 1 || person == null) {
								persons = new HashMap<String, Contact>();
								if (person != null) {
									persons.put(uid.toLowerCase(), person);
								}
							}
						}
						size = persons.size();
						sendPersons(currentMessageId, dn.substring(dn.indexOf(',')), persons, returningAttributes);
					} else {
						LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_ANONYMOUS_ACCESS_FORBIDDEN", currentMessageId, dn);
					}
				} else {
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_INVALID_DN", currentMessageId, dn);
				}
			} else if (LdapConnection.COMPUTER_CONTEXT.equals(dn) || LdapConnection.COMPUTER_CONTEXT_LION.equals(dn)) {
				size = 1;
				// computer context for iCal
				// computer context for iCal
				ldapConnection.sendComputerContext(currentMessageId, returningAttributes);
			} else if ((LdapConnection.BASE_CONTEXT.equalsIgnoreCase(dn) || LdapConnection.OD_USER_CONTEXT.equalsIgnoreCase(dn)) || LdapConnection.OD_USER_CONTEXT_LION.equalsIgnoreCase(dn)) {
				if (ldapConnection.getUser() != null) {
					Map<String, Contact> persons = new HashMap<String, Contact>();
					if (ldapFilter.isFullSearch()) {
						// append personal contacts first
						Map<String, Contact> contacts = contactFind(null, returningAttributes, sizeLimit);
						LogUtils.debug(log, "fullSearch: results:", contacts.size());						
						for (Contact person : contacts.values()) {
							persons.put(person.get("imapUid"), person);
							if (persons.size() == sizeLimit) {
								break;
							}
						}
						
						// DISABLED: appears to be just be away of loading results
						// from a user local list and a global address list, but we
						// probably don't need that distinction
						
						// full search
//						for (char c = 'A'; c <= 'Z'; c++) {
//							if (!abandon && persons.size() < sizeLimit) {
//								Collection<Contact> contacts = ldapConnection.getUser().galFind(Conditions.startsWith("cn", String.valueOf(c)), LdapUtils.convertLdapToContactReturningAttributes(returningAttributes), sizeLimit).values();
//								LogUtils.debug(log, "doSearch: results:", contacts.size());
//								for (Contact person : contacts) {
//									persons.put(person.get("uid"), person);
//									if (persons.size() == sizeLimit) {
//										break;
//									}
//								}
//							}
//							if (persons.size() == sizeLimit) {
//								break;
//							}
//						}
					} else {										
						// append only personal contacts
						Condition filter = ldapFilter.getContactSearchFilter();
						 //if ldapfilter is not a full search and filter is null,
						 //ignored all attribute filters => return empty results
						if (ldapFilter.isFullSearch() || filter != null) {
							for (Contact person : contactFind(filter, returningAttributes, sizeLimit).values()) {
								persons.put(person.get("imapUid"), person);
								if (persons.size() == sizeLimit) {
									break;
								}
							}
							if (!abandon && persons.size() < sizeLimit) {
								for (Contact person : ldapFilter.findInGAL(ldapConnection.getUser(), returningAttributes, sizeLimit - persons.size()).values()) {
									if (persons.size() == sizeLimit) {
										break;
									}
									persons.put(person.get("uid"), person);
								}
							}
						}
					}
					size = persons.size();
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_FOUND_RESULTS", currentMessageId, size);
					sendPersons(currentMessageId, ", " + dn, persons, returningAttributes);
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_END", currentMessageId);
				} else {
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_ANONYMOUS_ACCESS_FORBIDDEN", currentMessageId, dn);
				}
			} else if (dn != null && dn.length() > 0 && !LdapConnection.OD_CONFIG_CONTEXT.equals(dn) && !LdapConnection.OD_GROUP_CONTEXT.equals(dn)) {
				LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_INVALID_DN", currentMessageId, dn);
			}
			// iCal: do not send LDAP_SIZE_LIMIT_EXCEEDED on apple-computer search by cn with sizelimit 1
			// iCal: do not send LDAP_SIZE_LIMIT_EXCEEDED on apple-computer search by cn with sizelimit 1
			if (size > 1 && size == sizeLimit) {
				LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SIZE_LIMIT_EXCEEDED", currentMessageId);
				ldapConnection.sendClient(currentMessageId, LdapConnection.LDAP_REP_RESULT, LdapConnection.LDAP_SIZE_LIMIT_EXCEEDED, "");
			} else {
				LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SUCCESS", currentMessageId);
				ldapConnection.sendClient(currentMessageId, LdapConnection.LDAP_REP_RESULT, LdapConnection.LDAP_SUCCESS, "");
			}
		} catch (SocketException e) {
			// client closed connection
			// client closed connection
			log.warn("closed connection", e);
		} catch (IOException e) {
			log.error("", e);
			try {
				ldapConnection.sendErr(currentMessageId, LdapConnection.LDAP_REP_RESULT, e);
			} catch (IOException e2) {
				LogUtils.debug(log, "LOG_EXCEPTION_SENDING_ERROR_TO_CLIENT", e2);
			}
		} finally {
			synchronized (ldapConnection.searchThreadMap) {
				ldapConnection.searchThreadMap.remove(currentMessageId);
			}
		}
	}

	/**
	 * Search users in contacts folder
	 *
	 * @param condition           search filter
	 * @param returningAttributes requested attributes
	 * @param maxCount            maximum item count
	 * @return List of users
	 * @throws IOException on error
	 */
	public Map<String, Contact> contactFind(Condition condition, Set<String> returningAttributes, int maxCount) throws IOException {
		Map<String, Contact> results = new HashMap<String, Contact>();
		Set<String> contactReturningAttributes = LdapUtils.convertLdapToContactReturningAttributes(returningAttributes);
		contactReturningAttributes.remove("apple-serviceslocator");
		List<Contact> contacts = ldapConnection.getUser().searchContacts(contactReturningAttributes, condition, maxCount);
		for (Contact contact : contacts) {
			// use imapUid as uid
			// use imapUid as uid
			String imapUid = contact.get("imapUid");
			if (imapUid != null) {
				results.put(imapUid, contact);
			}
		}
		return results;
	}

	/**
	 * Convert to LDAP attributes and send entry
	 *
	 * @param currentMessageId    current Message Id
	 * @param baseContext         request base context (BASE_CONTEXT or OD_BASE_CONTEXT)
	 * @param persons             persons Map
	 * @param returningAttributes returning attributes
	 * @throws IOException on error
	 */
	/**
	 * Convert to LDAP attributes and send entry
	 *
	 * @param currentMessageId    current Message Id
	 * @param baseContext         request base context (BASE_CONTEXT or OD_BASE_CONTEXT)
	 * @param persons             persons Map
	 * @param returningAttributes returning attributes
	 * @throws IOException on error
	 */
	protected void sendPersons(int currentMessageId, String baseContext, Map<String, Contact> persons, Set<String> returningAttributes) throws IOException {
		LogUtils.debug(log, "sendPersons", baseContext, persons.size());
		boolean needObjectClasses = returningAttributes.contains("objectclass") || returningAttributes.isEmpty();
		boolean returnAllAttributes = returningAttributes.isEmpty();
		if( persons.isEmpty()) {
			log.warn("No contacts to send! -------------------");
		}
		for (Contact person : persons.values()) {
			if (abandon) {
				log.warn("Abandon flag is set, so exiting send!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				break;
			}
			Map<String, Object> ldapPerson = new HashMap<String, Object>();
			// convert Contact entries
			// convert Contact entries
			if (returnAllAttributes) {
				// just convert contact attributes to default ldap names
				// just convert contact attributes to default ldap names
				for (Map.Entry<String, String> entry : person.entrySet()) {
					String ldapAttribute = LdapUtils.getLdapAttributeName(entry.getKey());
					String value = entry.getValue();
					if (value != null) {
						ldapPerson.put(ldapAttribute, value);
					}
				}
			} else {
				// always map uid
				// always map uid
				ldapPerson.put("uid", person.get("imapUid"));
				// iterate over requested attributes
				// iterate over requested attributes
				for (String ldapAttribute : returningAttributes) {
					String contactAttribute = LdapUtils.getContactAttributeName(ldapAttribute);
					String value = person.get(contactAttribute);
					if (value != null) {
						if (ldapAttribute.startsWith("birth")) {
							SimpleDateFormat parser = LdapUtils.getZuluDateFormat();
							Calendar calendar = Calendar.getInstance();
							try {
								calendar.setTime(parser.parse(value));
							} catch (ParseException e) {
								throw new IOException(e);
							}
							if ("birthday".equals(ldapAttribute)) {
								value = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
							} else if ("birthmonth".equals(ldapAttribute)) {
								value = String.valueOf(calendar.get(Calendar.MONTH) + 1);
							} else if ("birthyear".equals(ldapAttribute)) {
								value = String.valueOf(calendar.get(Calendar.YEAR));
							}
						}
						ldapPerson.put(ldapAttribute, value);
					}
				}
			}
			// Process all attributes which have static mappings
			// Process all attributes which have static mappings
			for (Map.Entry<String, String> entry : LdapConnection.STATIC_ATTRIBUTE_MAP.entrySet()) {
				String ldapAttribute = entry.getKey();
				String value = entry.getValue();
				if (value != null && (returnAllAttributes || returningAttributes.contains(ldapAttribute))) {
					ldapPerson.put(ldapAttribute, value);
				}
			}
			if (needObjectClasses) {
				ldapPerson.put("objectClass", LdapConnection.PERSON_OBJECT_CLASSES);
			}
			// iCal: copy email to apple-generateduid, encode @
			// iCal: copy email to apple-generateduid, encode @
			if (returnAllAttributes || returningAttributes.contains("apple-generateduid")) {
				String mail = (String) ldapPerson.get("mail");
				if (mail != null) {
					ldapPerson.put("apple-generateduid", mail.replaceAll("@", "__AT__"));
				} else {
					// failover, should not happen
					// failover, should not happen
					ldapPerson.put("apple-generateduid", ldapPerson.get("uid"));
				}
			}
			// iCal: replace current user alias with login name
			// iCal: replace current user alias with login name
			if (ldapConnection.getUser().getAlias().equals(ldapPerson.get("uid"))) {
				if (returningAttributes.contains("uidnumber")) {
					ldapPerson.put("uidnumber", ldapConnection.getUserName());
				}
			}
			LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SEND_PERSON", currentMessageId, ldapPerson.get("uid"), baseContext, ldapPerson);
			ldapConnection.sendEntry(currentMessageId, "uid=" + ldapPerson.get("uid") + baseContext, ldapPerson);
		}
	}

}
