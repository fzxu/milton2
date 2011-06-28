/* MetafileReader.java

MetafileReader: Metafile reader class
Copyright (C) 2011 Tomáš Hlavnička <hlavntom@fel.cvut.cz>

This file is a part of Jazsync.

Jazsync is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

Jazsync is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jazsync; if not, write to the

Free Software Foundation, Inc.,
59 Temple Place, Suite 330,
Boston, MA  02111-1307
USA
 */
package jazsync.jazsync;

import com.bradmcevoy.io.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Security;
import jazsync.jazsync.HeaderService.Headers;

import org.jarsync.ChecksumPair;
import org.jarsync.JarsyncProvider;

/**
 * Class used to read metafile
 * @author Tomáš Hlavnička
 */
public class MetaFileReader {

	private ChainingHash hashtable;
	private int fileOffset;
	private int blockNum;
	/** Variables for header information from .zsync metafile */
	//------------------------------

	//------------------------------
	private String url;
	private String extraInputFile;
	private int ranges = 100;
	private long downloadedMetafile = 0;

	/** Option variables */
	/** Option variables */
	/**
	 * Metafile constructor
	 * @param args Arguments
	 */
	public MetaFileReader() {
		Security.addProvider(new JarsyncProvider());
	}

	public void read(Headers headers, InputStream in) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		StreamUtils.readTo(in, bout);
		blockNum = (int) Math.ceil((double) headers.getLength() / (double) headers.getBlockSize());
		fillHashTable(headers, bout.toByteArray());
	}


	/**
	 * Fills a chaining hash table with ChecksumPairs
	 * @param checksums Byte array with bytes of whole metafile
	 */
	private void fillHashTable(Headers headers, byte[] checksums) {
		int i = 16;
		//spocteme velikost hashtable podle poctu bloku dat
		while ((2 << (i - 1)) > blockNum && i > 4) {
			i--;
		}
		//vytvorime hashtable o velikosti 2^i (max. 2^16, min. 2^4)
		hashtable = new ChainingHash(2 << (i - 1));
		ChecksumPair p = null;
		//Link item;
		int offset = 0;
		int weakSum = 0;
		int seq = 0;
		int off = fileOffset;

		byte[] weak = new byte[4];
		byte[] strongSum = new byte[headers.mf_checksum_bytes];

		while (seq < blockNum) {

			for (int w = 0; w < headers.mf_rsum_bytes; w++) {
				weak[w] = checksums[off];
				off++;
			}

			for (int s = 0; s < strongSum.length; s++) {
				strongSum[s] = checksums[off];
				off++;
			}

			weakSum = 0;
			weakSum += (weak[2] & 0x000000FF) << 24;
			weakSum += (weak[3] & 0x000000FF) << 16;
			weakSum += (weak[0] & 0x000000FF) << 8;
			weakSum += (weak[1] & 0x000000FF);

			p = new ChecksumPair(weakSum, strongSum.clone(), offset, headers.getBlockSize(), seq);
			offset += headers.getBlockSize();
			seq++;
			//item = new Link(p);
			hashtable.insert(p);
		}
	}

	public ChainingHash getHashtable() {
		return hashtable;
	}

	/**
	 * Returns number of blocks in complete file
	 * @return Number of blocks
	 */
	public int getBlockCount() {
		return blockNum;
	}

	/**
	 * Return URL as origin of local metafile (in case that metafile contains
	 * relative URL to a file)
	 * @return URL address in String format
	 */
	public String getRelativeURL() {
		return url;
	}

	/**
	 * Maximum number of simultaneously downloaded blocks
	 * @return Max. number of blocks downloaded in one piece
	 */
	public int getRangesNumber() {
		return ranges;
	}

	/**
	 * Returns filename of seeding file
	 * @return Filename of extra seeding file
	 */
	public String getInputFile() {
		return extraInputFile;
	}

	/**
	 * Length of DOWNLOADED metafile
	 * @return Length of metafile
	 */
	public long getLengthOfMetafile() {
		return downloadedMetafile;
	}

	public String getMtime() {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}