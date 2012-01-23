package info.ineighborhood.cardme.vcard.features;

import java.util.Iterator;

/**
 * Copyright (c) 2004, Neighborhood Technologies
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Neighborhood Technologies nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * 
 * @author George El-Haddad
 * <br/>
 * Feb 4, 2010
 * 
 * <p><b>RFC 2426</b><br/>
 * <b>3.5.5 ORG Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> ORG</li>
 * 	<li><b>Type purpose:</b> To specify the organizational name and units associated with the vCard.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single structured text value consisting of components separated the SEMI-COLON character (ASCII decimal 59).</li>
 * 	<li><b>Type special note:</b> The type is based on the X.520 Organization Name and Organization Unit attributes. The type value is a structured type consisting of the organization name, followed by one or more levels of organizational unit names.</li>
 * </ul>
 * </p>
 */
public interface OrganizationFeature extends TypeTools {
	
	/**
	 * <p>Adds an organization to this vcard.</p>
	 * 
	 * @param organization
	 */
	public void addOrganization(String organization);
	
	/**
	 * <p>Removes the specified organization from this vcard.</p>
	 * 
	 * @param organization
	 */
	public void removeOrganization(String organization);
	
	/**
	 * <p>Returns true if the specified organization exists.</p>
	 * 
	 * @param organization
	 * @return boolean
	 */
	public boolean containsOrganization(String organization);
	
	/**
	 * <p>Returns an iterator of all organizations.</p>
	 *
	 * @return {@link Iterator}&lt;String&gt;
	 */
	public Iterator<String> getOrganizations();
	
	/**
	 * <p>Clears all organizations from this vcard.</p>
	 */
	public void clearOrganizations();
	
	/**
	 * <p>Returns true if this vcard has organizations.</p>
	 * 
	 * @return boolean
	 */
	public boolean hasOrganizations();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link OrganizationFeature}
	 */
	public OrganizationFeature clone();
}
