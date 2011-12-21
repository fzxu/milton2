package com.ettrema.ldap;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class LdapUtils {

	private static final Logger log = LoggerFactory.getLogger(LdapUtils.class);
	
	static SimpleDateFormat getZuluDateFormat() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/**
	 * Convert LDAP attribute name to contact attribute name.
	 *
	 * @param contactAttributeName ldap attribute name
	 * @return contact attribute name
	 */
	public static String getLdapAttributeName(String contactAttributeName) {
		String mappedAttributeName = LdapConnection.CONTACT_TO_LDAP_ATTRIBUTE_MAP.get(contactAttributeName);
		if (mappedAttributeName != null) {
			return mappedAttributeName;
		} else {
			return contactAttributeName;
		}
	}

	public static Set<String> convertLdapToContactReturningAttributes(Set<String> returningAttributes) {
		Set<String> contactReturningAttributes;
		if (returningAttributes != null && !returningAttributes.isEmpty()) {
			contactReturningAttributes = new HashSet<String>();
			// always return uid
			contactReturningAttributes.add("imapUid");
			for (String attribute : returningAttributes) {
				String contactAttributeName = getContactAttributeName(attribute);
				if (contactAttributeName != null) {
					contactReturningAttributes.add(contactAttributeName);
				}
			}
		} else {
			contactReturningAttributes = ContactAttributes.CONTACT_ATTRIBUTES;
		}
		return contactReturningAttributes;
	}	
	
    /**
     * Convert contact attribute name to LDAP attribute name.
     *
     * @param ldapAttributeName ldap attribute name
     * @return contact attribute name
     */
    public static String getContactAttributeName(String ldapAttributeName) {
		String contactAttributeName = null;
		// first look in contact attributes
		if (ContactAttributes.CONTACT_ATTRIBUTES.contains(ldapAttributeName)) {
			contactAttributeName = ldapAttributeName;
		} else if (LdapConnection.LDAP_TO_CONTACT_ATTRIBUTE_MAP.containsKey(ldapAttributeName)) {
			String mappedAttribute = LdapConnection.LDAP_TO_CONTACT_ATTRIBUTE_MAP.get(ldapAttributeName);
			if (mappedAttribute != null) {
				contactAttributeName = mappedAttribute;
			}
		} else {
			log.debug("UNKNOWN_ATTRIBUTE", ldapAttributeName);
        }
        return contactAttributeName;
	}
	
}
