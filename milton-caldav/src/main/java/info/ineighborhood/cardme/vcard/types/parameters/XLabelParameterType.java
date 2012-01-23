package info.ineighborhood.cardme.vcard.types.parameters;

import info.ineighborhood.cardme.vcard.VCardType;

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
 * Aug 23, 2010
 *
 */
public class XLabelParameterType extends XTendedParameterType {

	/**
	 * <p>Creates a new extended label parameter type with
	 * the extended name and value.</p>
	 * <p>Example: <code>LABEL=X-EVOLUTION-SLOT=1;</code></p>
	 * 
	 * @param xtendedTypeName
	 * @param xtendedTypeValue
	 */
	public XLabelParameterType(String xtendedTypeName, String xtendedTypeValue) {
		super(xtendedTypeName, xtendedTypeValue);
	}
	
	/**
	 * <p>Creates a new extended label parameter type with
	 * the extended name only.
	 * <p>Example: <code>EMAIL;TYPE=X-COLOR-RED;</code></p>
	 * 
	 * @param xtendedTypeName
	 */
	public XLabelParameterType(String xtendedTypeName) {
		super(xtendedTypeName);
	}
	
	@Override
	public String getType()
	{
		return LabelParameterType.NON_STANDARD.getType();
	}
	
	@Override
	public String getDescription()
	{
		return LabelParameterType.NON_STANDARD.getDescription();
	}
	
	@Override
	public VCardType getParentType()
	{
		return VCardType.LABEL;
	}
}
