package com.ettrema.ldap;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.values.ValueAndType;
import com.bradmcevoy.property.PropertySource;
import com.ettrema.common.LogUtils;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import javax.xml.namespace.QName;
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
    private final List<PropertySource> propertySources;

    protected SearchRunnable(UserFactory userFactory, List<PropertySource> propertySources, int currentMessageId, String dn, int scope, int sizeLimit, int timelimit, LdapConnection.LdapFilter ldapFilter, Set<String> returningAttributes, final LdapConnection ldapConnection) {
        this.userFactory = userFactory;
        this.propertySources = propertySources;
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
    public void abandon() {
        abandon = true;
    }

    @Override
    public void run() {
        try {
            int size = 0;
            LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH", currentMessageId, "dn:", dn, "scope:", scope, "size:", sizeLimit, timelimit, ldapFilter.toString(), returningAttributes);
            if (scope == LdapConnection.SCOPE_BASE_OBJECT) {
                if ("".equals(dn)) {
                    size = 1;
                    ldapConnection.sendRootDSE(currentMessageId);
                } else if (LdapConnection.BASE_CONTEXT.equals(dn) ) {
                    size = 1;
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
                            List<Contact> galContacts = userFactory.galFind(Conditions.isEqualTo("imapUid", uid), LdapUtils.convertLdapToContactReturningAttributes(returningAttributes), sizeLimit);
                            if (galContacts != null && galContacts.size() > 0) {
                                Contact person = galContacts.get(0);
                                if (persons == null) {
                                    persons = new HashMap<String, Contact>();
                                }
                                persons.put(uid.toLowerCase(), person);
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
                ldapConnection.sendComputerContext(currentMessageId, returningAttributes);
            } else if ((LdapConnection.BASE_CONTEXT.equalsIgnoreCase(dn) 
                    || LdapConnection.OD_USER_CONTEXT.equalsIgnoreCase(dn)) 
                    || LdapConnection.MSLIVE_BASE_CONTEXT.equals(dn)
                    || LdapConnection.OD_USER_CONTEXT_LION.equalsIgnoreCase(dn)) {
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

                        // full search
                        for (char c = 'A'; c <= 'Z'; c++) {
                            if (!abandon && persons.size() < sizeLimit) {
                                Set<String> atts = LdapUtils.convertLdapToContactReturningAttributes(returningAttributes);
                                Condition startsWith = Conditions.startsWith("cn", String.valueOf(c));
                                Collection<Contact> galContacts = userFactory.galFind(startsWith, atts, sizeLimit);
                                LogUtils.debug(log, "doSearch: results:", contacts.size());
                                for (Contact person : galContacts) {
                                    persons.put(person.getUniqueId(), person);
                                    if (persons.size() == sizeLimit) {
                                        break;
                                    }
                                }
                            }
                            if (persons.size() == sizeLimit) {
                                break;
                            }
                        }
                    } else {
                        // append only personal contacts
                        Condition filter = ldapFilter.getContactSearchFilter();
                        LogUtils.debug(log, "not full search:", filter);
                        //if ldapfilter is not a full search and filter is null,
                        //ignored all attribute filters => return empty results
                        if (ldapFilter.isFullSearch() || filter != null) {
                            for (Contact person : contactFind(filter, returningAttributes, sizeLimit).values()) {
                                persons.put(person.get("imapUid"), person);
                                if (persons.size() == sizeLimit) {
                                    log.debug("EXceeded size limit1");
                                    break;
                                }
                            }
                            LogUtils.trace(log, "local contacts result size: ", persons.size());
                            if (!abandon && persons.size() < sizeLimit) {
                                List<Contact> galContacts = ldapFilter.findInGAL(ldapConnection.getUser(), returningAttributes, sizeLimit - persons.size());
                                LogUtils.trace(log, "gal contacts result size: ", galContacts.size());
                                for (Contact person : galContacts) {
                                    if (persons.size() >= sizeLimit) {
                                        log.debug("EXceeded size limit2");
                                        break;
                                    }
                                    LogUtils.trace(log, "add contact to results: ", person.getUniqueId());
                                    persons.put(person.getUniqueId(), person);
                                }
                            }
                        }
                    }
                    LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_FOUND_RESULTS", currentMessageId, persons.size());
                    sendPersons(currentMessageId, ", " + dn, persons, returningAttributes);
                    LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_END", currentMessageId);
                } else {
                    LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_ANONYMOUS_ACCESS_FORBIDDEN", currentMessageId, dn);
                }
            } else if (dn != null && dn.length() > 0 && !LdapConnection.OD_CONFIG_CONTEXT.equals(dn) && !LdapConnection.OD_GROUP_CONTEXT.equals(dn)) {
                LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_INVALID_DN2", currentMessageId, dn);
            }
            // iCal: do not send LDAP_SIZE_LIMIT_EXCEEDED on apple-computer search by cn with sizelimit 1
            if (size > 1 && size == sizeLimit) {
                LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SIZE_LIMIT_EXCEEDED", currentMessageId);
                ldapConnection.sendClient(currentMessageId, LdapConnection.LDAP_REP_RESULT, LdapConnection.LDAP_SIZE_LIMIT_EXCEEDED, "");
            } else {
                LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SUCCESS", currentMessageId, "size:", size);
                ldapConnection.sendClient(currentMessageId, LdapConnection.LDAP_REP_RESULT, LdapConnection.LDAP_SUCCESS, "");
            }
        } catch (SocketException e) {
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
     * @param condition search filter
     * @param returningAttributes requested attributes
     * @param maxCount maximum item count
     * @return List of users
     * @throws IOException on error
     */
    public Map<String, Contact> contactFind(Condition condition, Set<String> returningAttributes, int maxCount) throws IOException {
        Map<String, Contact> results = new HashMap<String, Contact>();
        Set<String> contactReturningAttributes = LdapUtils.convertLdapToContactReturningAttributes(returningAttributes);
        contactReturningAttributes.remove("apple-serviceslocator");
        List<Contact> contacts = ldapConnection.getUser().searchContacts(contactReturningAttributes, condition, maxCount);
        LogUtils.trace(log, "contactFind: contacts size:", contacts.size());
        for (Contact contact : contacts) {
            String imapUid = contact.get("imapUid");
            if (imapUid != null) {
                results.put(imapUid, contact);
            } else {
                log.warn("Not including contact because imapUid field is null: " + contact);
            }
        }
        return results;
    }

    private void sendPersons(int currentMessageId, String baseContext, Map<String, Contact> persons, Set<String> returningAttributes) throws IOException {
        LogUtils.debug(log, "sendPersons", baseContext, "size:", persons.size());
        boolean needObjectClasses = returningAttributes.contains("objectclass") || returningAttributes.isEmpty();
        boolean returnAllAttributes = returningAttributes.isEmpty();
        if (persons.isEmpty()) {
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
                for (Map.Entry<String, String> entry : person.entrySet()) {
                    String ldapAttribute = entry.getKey();
                    String value = entry.getValue();
                    if (value != null) {
                        ldapPerson.put(ldapAttribute, value);
                    }
                }
            } else {
                // always map uid
                ldapPerson.put("uid", person.getUniqueId());
                // iterate over requested attributes
                for (String ldapAttribute : returningAttributes) {
                    String contactAttribute = ldapAttribute;
                    String value = person.get(contactAttribute);
                    if (value != null) {
//						if (ldapAttribute.startsWith("birth")) {
//							SimpleDateFormat parser = LdapUtils.getZuluDateFormat();
//							Calendar calendar = Calendar.getInstance();
//							try {
//								calendar.setTime(parser.parse(value));
//							} catch (ParseException e) {
//								throw new IOException(e);
//							}
//							if ("birthday".equals(ldapAttribute)) {
//								value = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
//							} else if ("birthmonth".equals(ldapAttribute)) {
//								value = String.valueOf(calendar.get(Calendar.MONTH) + 1);
//							} else if ("birthyear".equals(ldapAttribute)) {
//								value = String.valueOf(calendar.get(Calendar.YEAR));
//							}
//						}
                        ldapPerson.put(ldapAttribute, value);
                    }
                }
            }
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
            if (ldapConnection.getUser().getAlias().equals(ldapPerson.get("uid"))) {
                if (returningAttributes.contains("uidnumber")) {
                    ldapPerson.put("uidnumber", ldapConnection.getUserName());
                }
            }
            LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SEND_PERSON", currentMessageId, ldapPerson.get("uid"), baseContext, ldapPerson);
            ldapConnection.sendEntry(currentMessageId, "uid=" + ldapPerson.get("uid") + baseContext, ldapPerson);
        }
    }

    private ValueAndType getProperty(QName field, Resource resource) throws NotAuthorizedException {
        log.debug("num property sources: " + propertySources.size());
        for (PropertySource source : propertySources) {
            PropertySource.PropertyMetaData meta = source.getPropertyMetaData(field, resource);
            if (meta != null && !meta.isUnknown()) {
                Object val = source.getProperty(field, resource);
                return new ValueAndType(val, meta.getValueType());
            }
        }
        return null;
    }

    private String getPropertyValue(String ldapName, Resource resource) {
        return null;
        // TODO: use this instead of map on User/Contact
    }
}
