package com.ettrema.ldap;

/**
 *
 * @author brad
 */
public interface User {

	/**
	 * Not really sure what this is for, but i think you can just return the username
	 * 
	 * @return 
	 */
	public String getAlias();

	
    /**
	 * No, i don't know why this is here
	 * 
     * And search filter.
     *
     * @param condition search conditions
     * @return condition
     */
    public abstract MultiCondition and(Condition... condition);

    /**
	 * No, i don't know why this is here
	 * 
     * Or search filter.
     *
     * @param condition search conditions
     * @return condition
     */
    public abstract MultiCondition or(Condition... condition);

	public Condition isEqualTo(String contactAttributeName, String value);

	public Condition contains(String contactAttributeName, String value);

	public Condition startsWith(String contactAttributeName, String value);
}
