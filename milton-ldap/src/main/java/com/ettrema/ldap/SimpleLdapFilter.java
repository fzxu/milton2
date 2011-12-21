package com.ettrema.ldap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
class SimpleLdapFilter implements LdapConnection.LdapFilter {
	private static final Logger log = LoggerFactory.getLogger(SimpleLdapFilter.class);
	
	static final String STAR = "*";
	final String attributeName;
	final String value;
	final int mode;
	final int operator;
	final boolean canIgnore;

	SimpleLdapFilter(String attributeName) {
		this.attributeName = attributeName;
		this.value = SimpleLdapFilter.STAR;
		this.operator = LdapConnection.LDAP_FILTER_SUBSTRINGS;
		this.mode = 0;
		this.canIgnore = checkIgnore();
	}

	SimpleLdapFilter(String attributeName, String value, int ldapFilterOperator, int ldapFilterMode) {
		this.attributeName = attributeName;
		this.value = value;
		this.operator = ldapFilterOperator;
		this.mode = ldapFilterMode;
		this.canIgnore = checkIgnore();
	}

	private boolean checkIgnore() {
		if ("objectclass".equals(attributeName) && STAR.equals(value)) {
			// ignore cases where any object class can match
			return true;
		} else if (LdapConnection.IGNORE_MAP.contains(attributeName)) {
			// Ignore this specific attribute
			return true;
		} else if (LdapConnection.CRITERIA_MAP.get(attributeName) == null && LdapUtils.getContactAttributeName(attributeName) == null) {
			log.debug("LOG_LDAP_UNSUPPORTED_FILTER_ATTRIBUTE", attributeName, value);
			return true;
		}
		return false;
	}

	@Override
	public boolean isFullSearch() {
		// only (objectclass=*) is a full search
		return "objectclass".equals(attributeName) && STAR.equals(value);
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append('(');
		buffer.append(attributeName);
		buffer.append('=');
		if (SimpleLdapFilter.STAR.equals(value)) {
			buffer.append(SimpleLdapFilter.STAR);
		} else if (operator == LdapConnection.LDAP_FILTER_SUBSTRINGS) {
			if (mode == LdapConnection.LDAP_SUBSTRING_FINAL || mode == LdapConnection.LDAP_SUBSTRING_ANY) {
				buffer.append(SimpleLdapFilter.STAR);
			}
			buffer.append(value);
			if (mode == LdapConnection.LDAP_SUBSTRING_INITIAL || mode == LdapConnection.LDAP_SUBSTRING_ANY) {
				buffer.append(SimpleLdapFilter.STAR);
			}
		} else {
			buffer.append(value);
		}
		buffer.append(')');
		return buffer.toString();
	}

	@Override
	public Condition getContactSearchFilter() {
		String contactAttributeName = LdapUtils.getContactAttributeName(attributeName);
		if (canIgnore || (contactAttributeName == null)) {
			return null;
		}
		Condition condition = null;
		if (operator == LdapConnection.LDAP_FILTER_EQUALITY) {
			condition = Conditions.isEqualTo(contactAttributeName, value);
		} else if ("*".equals(value)) {
			condition = Conditions.not(Conditions.isNull(contactAttributeName));
			// do not allow substring search on integer field imapUid
		} else if (!"imapUid".equals(contactAttributeName)) {
			// endsWith not supported by exchange, convert to contains
			if (mode == LdapConnection.LDAP_SUBSTRING_FINAL || mode == LdapConnection.LDAP_SUBSTRING_ANY) {
				condition = Conditions.contains(contactAttributeName, value);
			} else {
				condition = Conditions.startsWith(contactAttributeName, value);
			}
		}
		return condition;
	}

	@Override
	public boolean isMatch(Map<String, String> person) {
		if (canIgnore) {
			// Ignore this filter
			return true;
		}
		String personAttributeValue = person.get(attributeName);
		if (personAttributeValue == null) {
			// No value to allow for filter match
			return false;
		} else if (value == null) {
			// This is a presence filter: found
			return true;
		} else if ((operator == LdapConnection.LDAP_FILTER_EQUALITY) && personAttributeValue.equalsIgnoreCase(value)) {
			// Found an exact match
			return true;
		} else if ((operator == LdapConnection.LDAP_FILTER_SUBSTRINGS) && (personAttributeValue.toLowerCase().indexOf(value.toLowerCase()) >= 0)) {
			// Found a substring match
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Contact> findInGAL(User user, Set<String> returningAttributes, int sizeLimit) throws IOException {
		if (canIgnore) {
			return null;
		}
		String contactAttributeName = LdapUtils.getContactAttributeName(attributeName);
		if (contactAttributeName != null) {
			// quick fix for cn=* filter
			Map<String, Contact> galPersons = user.galFind(Conditions.startsWith(contactAttributeName, "*".equals(value) ? "A" : value), LdapUtils.convertLdapToContactReturningAttributes(returningAttributes), sizeLimit);
			if (operator == LdapConnection.LDAP_FILTER_EQUALITY) {
				// Make sure only exact matches are returned
				Map<String, Contact> results = new HashMap<String, Contact>();
				for (Contact person : galPersons.values()) {
					if (isMatch(person)) {
						// Found an exact match
						results.put(person.get("uid"), person);
					}
				}
				return results;
			} else {
				return galPersons;
			}
		}
		return null;
	}

	@Override
	public void add(LdapConnection.LdapFilter filter) {
		// Should never be called
		log.error("LOG_LDAP_UNSUPPORTED_FILTER", "nested simple filters");
	}
	
}
