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
 * <b>3.6.3 PRODID Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> PRODID</li>
 * 	<li><b>Type purpose:</b> To specify the identifier for the product that created the vCard object.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single text value.</li>
 * 	<li><b>Type special note:</b> Implementations SHOULD use a method such as that specified for Formal Public Identifiers in ISO 9070 to assure that the text value is unique.</li>
 * </ul>
 * </p>
 */
public interface ProductIdFeature extends TypeTools {
	
	/**
	 * <p>Returns the product id.</p>
	 *
	 * @return {@link String}
	 */
	public String getProductId();
	
	/**
	 * <p>Sets the product id.</p>
	 *
	 * @param productId
	 */
	public void setProductId(String productId);
	
	/**
	 * <p>Clears the product id.</p>
	 */
	public void clearProductId();
	
	/**
	 * <p>Returns true if the product id exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasProductId();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link ProductIdFeature}
	 */
	public ProductIdFeature clone();
}
