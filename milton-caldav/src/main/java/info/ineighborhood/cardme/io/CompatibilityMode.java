package info.ineighborhood.cardme.io;

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
 * Feb 8, 2010
 *
 */
public enum CompatibilityMode {

	/**
	 * <p>Pure RFC-2426 compatibility.</p>
	 */
	RFC2426,
	
	/**
	 * <p>Microsoft Outlook 2003 & 2007 use vcard 2.1 format with
	 * non-standard tags and formatting.</p>
	 */
	MS_OUTLOOK,
	
	/**
	 * <p>iPhone exported vcards
	 * 	<ol>
	 * 		<li>Encoding parameter type uses BASE64 instead of B.</li>
	 * 		<li>Does not include the ENCODING parameter type for PHOTOs.</li>
	 * 	</ol>
	 * </p>
	 */
	I_PHONE,
	
	/**
	 * <p>
	 * 	<ol>
	 * 		<li>Folds lines at 76 characters instead of 75.</li>
	 * 	</ol>
	 * </p>
	 */
	MAC_ADDRESS_BOOK,
	
	/**
	 * <p>Compatibility mode for use with the KDE Address Book application.
	 * 	<ol>
	 * 		<li>Uses escaped commas in the CATEGORIES feature when there
	 * 		    is more than one category. The RFC-2426 explicitly states
	 * 		    &quot;One or more text values separated by a COMMA character&quot;</li>
	 * 	</ol>
	 * </p>
	 */
	KDE_ADDRESS_BOOK,
	
	EVOLUTION;
}
