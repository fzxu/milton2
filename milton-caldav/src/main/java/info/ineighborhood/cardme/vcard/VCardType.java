package info.ineighborhood.cardme.vcard;

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
 * Feb 5, 2010
 *
 */
public enum VCardType {
	BEGIN("BEGIN"),
	END("END"),
	NAME("NAME"),
	PROFILE("PROFILE"),
	SOURCE("SOURCE"),
	FN("FN"),
	N("N"),
	NICKNAME("NICKNAME"),
	PHOTO("PHOTO"),
	BDAY("BDAY"),
	ADR("ADR"),
	LABEL("LABEL"),
	TEL("TEL"),
	EMAIL("EMAIL"),
	MAILER("MAILER"),
	TZ("TZ"),
	GEO("GEO"),
	TITLE("TITLE"),
	ROLE("ROLE"),
	LOGO("LOGO"),
	AGENT("AGENT"),
	ORG("ORG"),
	CATEGORIES("CATEGORIES"),
	NOTE("NOTE"),
	PRODID("PRODID"),
	REV("REV"),
	SORT_STRING("SORT-STRING"),
	SOUND("SOUND"),
	UID("UID"),
	URL("URL"),
	VERSION("VERSION"),
	CLASS("CLASS"),
	KEY("KEY"),
	XTENDED("X-");

	private String type = null;
	VCardType(String _type) {
		this.type = _type;
	}

	public String getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		return type;
	}
}
