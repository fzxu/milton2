package com.ettrema.ldap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author brad
 */
public class CompoundLdapFilter implements LdapConnection.LdapFilter {
	final Set<LdapConnection.LdapFilter> criteria = new HashSet<LdapConnection.LdapFilter>();
	final int type;

	CompoundLdapFilter(int filterType) {
		type = filterType;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (type == LdapConnection.LDAP_FILTER_OR) {
			buffer.append("(|");
		} else if (type == LdapConnection.LDAP_FILTER_AND) {
			buffer.append("(&");
		} else {
			buffer.append("(!");
		}
		for (LdapConnection.LdapFilter child : criteria) {
			buffer.append(child.toString());
		}
		buffer.append(')');
		return buffer.toString();
	}

	/**
	 * Add child filter
	 *
	 * @param filter inner filter
	 */
	public void add(LdapConnection.LdapFilter filter) {
		criteria.add(filter);
	}

	/**
	 * This is only a full search if every child
	 * is also a full search
	 *
	 * @return true if full search filter
	 */
	@Override
	public boolean isFullSearch() {
		for (LdapConnection.LdapFilter child : criteria) {
			if (!child.isFullSearch()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Build search filter for Contacts folder search.
	 * Use Exchange SEARCH syntax
	 *
	 * @return contact search filter
	 */
	public Condition getContactSearchFilter() {
		MultiCondition condition;
		if (type == LdapConnection.LDAP_FILTER_OR) {
			condition = Conditions.or();
		} else {
			condition = Conditions.and();
		}
		for (LdapConnection.LdapFilter child : criteria) {
			condition.add(child.getContactSearchFilter());
		}
		return condition;
	}

	/**
	 * Test if person matches the current filter.
	 *
	 * @param person person attributes map
	 * @return true if filter match
	 */
	@Override
	public boolean isMatch(Map<String, String> person) {
		if (type == LdapConnection.LDAP_FILTER_OR) {
			for (LdapConnection.LdapFilter child : criteria) {
				if (!child.isFullSearch()) {
					if (child.isMatch(person)) {
						// We've found a match
						return true;
					}
				}
			}
			// No subconditions are met
			return false;
		} else if (type == LdapConnection.LDAP_FILTER_AND) {
			for (LdapConnection.LdapFilter child : criteria) {
				if (!child.isFullSearch()) {
					if (!child.isMatch(person)) {
						// We've found a miss
						return false;
					}
				}
			}
			// All subconditions are met
			return true;
		}
		return false;
	}

	/**
	 * Find persons in Exchange GAL matching filter.
	 * Iterate over child filters to build results.
	 *
	 * @param user Exchange session
	 * @return persons map
	 * @throws IOException on error
	 */
	public Map<String, Contact> findInGAL(User user, Set<String> returningAttributes, int sizeLimit) throws IOException {
		Map<String, Contact> persons = null;
		for (LdapConnection.LdapFilter child : criteria) {
			int currentSizeLimit = sizeLimit;
			if (persons != null) {
				currentSizeLimit -= persons.size();
			}
			Map<String, Contact> childFind = child.findInGAL(user, returningAttributes, currentSizeLimit);
			if (childFind != null) {
				if (persons == null) {
					persons = childFind;
				} else if (type == LdapConnection.LDAP_FILTER_OR) {
					// Create the union of the existing results and the child found results
					persons.putAll(childFind);
				} else if (type == LdapConnection.LDAP_FILTER_AND) {
					// Append current child filter results that match all child filters to persons.
					// The hard part is that, due to the 100-item-returned galFind limit
					// we may catch new items that match all child filters in each child search.
					// Thus, instead of building the intersection, we check each result against
					// all filters.
					for (Contact result : childFind.values()) {
						if (isMatch(result)) {
							// This item from the child result set matches all sub-criteria, add it
							persons.put(result.get("uid"), result);
						}
					}
				}
			}
		}
		if ((persons == null) && !isFullSearch()) {
			// return an empty map (indicating no results were found)
			return new HashMap<String, Contact>();
		}
		return persons;
	}
	
}
