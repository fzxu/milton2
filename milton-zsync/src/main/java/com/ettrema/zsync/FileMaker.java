/* FileMaker.java

FileMaker: File reading and making class
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
package com.ettrema.zsync;

import java.io.File;






import java.util.Arrays;
import com.ettrema.zsync.MetaFileMaker.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Target file making class
 * @author Tomáš Hlavnička
 */
public class FileMaker {

	private static final Logger log = LoggerFactory.getLogger(FileMaker.class);
	
	private MapMatcher mapMatcher = new MapMatcher();
	private FileUpdater fileUpdater = new FileUpdater();
	private FileDownloader fileDownloader = new FileDownloader();

	public FileMaker() {
	}

	/**
	 * 
	 * @param inputFile - the "local" file, containing data which needs to be merged
	 * with that on the server
	 */
	public void make(File inputFile, File metafile, RangeLoader rangeLoader) throws Exception {
		MetaFileReader mfr = new MetaFileReader(metafile);
		make(mfr, inputFile, rangeLoader);
	}

	private void make(MetaFileReader mfr, File inputFile, RangeLoader rangeLoader) throws Exception {
		MakeContext makeContext = new MakeContext(mfr.getHashtable(), new long[mfr.getBlockCount()]);
		Arrays.fill(makeContext.fileMap, -1);
		double complete = mapMatcher.mapMatcher(inputFile, mfr, makeContext);
		File dest = File.createTempFile("zsyncM_", "_" + inputFile.getName());
		if (complete == 0) {
			fileDownloader.downloadWholeFile(rangeLoader, dest);
		} else {
			fileUpdater.update(inputFile, mfr, rangeLoader, makeContext, dest);
		}
		// TODO: move dest to inputFile
	}

	public void make(File fLocal, MetaData metaData, LocalFileRangeLoader rangeLoader) {
		MetaFileReader mfr = new MetaFileReader(metaData);
	}
}