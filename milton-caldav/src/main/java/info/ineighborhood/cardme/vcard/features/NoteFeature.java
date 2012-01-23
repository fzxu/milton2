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
 * @author Georges El-Haddad
 * <br/>
 * Aug 9, 2006
 * 
 * <p><b>RFC 2426</b><br/>
 * <b>3.6.2 NOTE Type Definition</b>
 * <ul>
 * 	<li><b>Type name:</b> NOTE</li>
 * 	<li><b>Type purpose:</b> To specify supplemental information or a comment that is associated with the vCard.</li>
 * 	<li><b>Type encoding:</b> 8bit</li>
 * 	<li><b>Type value:</b> A single text value.</li>
 * 	<li><b>Type special note:</b> The type is based on the X.520 Description attribute.</li>
 * </ul>
 * </p>
 */
public interface NoteFeature extends TypeTools {

	/**
	 * <p>Returns the note.</p>
	 *
	 * @return {@link String}
	 */
	public String getNote();
	
	/**
	 * <p>Sets the note.</p>
	 *
	 * @param note
	 */
	public void setNote(String note);
	
	/**
	 * <p>Returns true if a note exists.</p>
	 *
	 * @return boolean
	 */
	public boolean hasNote();
	
	/**
	 * <p>Returns a full copy of this object.</p>
	 *
	 * @return {@link NoteFeature}
	 */
	public NoteFeature clone();
}
