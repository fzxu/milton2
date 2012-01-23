package info.ineighborhood.cardme.vcard.features;

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
 * <b>3.5.2 ROLE Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> ROLE</li>
 * 	<li><b>Type purpose:</b> To specify information concerning the role, occupation, or business category of the object the vCard represents.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single text value.</li>
 * 	<li><b>Type special note:</b> This type is based on the X.520 Business Category explanatory attribute. This property is included as an organizational type to avoid confusion with the semantics of the TITLE type and incorrect usage of that type when the semantics of this type is intended.</li>
 * </ul>
 * </p>
 */
public interface RoleFeature extends TypeTools {
	
	/**
	 * <p>Returns the role.</p>
	 *
	 * @return {@link String}
	 */
	public String getRole();
	
	/**
	 * <p>Sets the role.</p>
	 *
	 * @param role
	 */
	public void setRole(String role);
	
	/**
	 * <p>Clears the role.</p>
	 */
	public void clearRole();
	
	/**
	 * <p>Returns true if the role exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasRole();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link RoleFeature}
	 */
	public RoleFeature clone();
}
