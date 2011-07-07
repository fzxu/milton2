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

import com.ettrema.http.DataRange;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad, original work by Tomáš Hlavnička
 */
public class FileUpdater {
	
	private static final Logger log = LoggerFactory.getLogger(FileUpdater.class);
	
	/**
	 * Maximum ranges to download in the range header
	 */
	private int maxRanges = 100;
	
	
	/**
	 * Method for completing file
	 */
	public  void update(File inputFile, MetaFileReader mfr, RangeLoader rangeLoader, MakeContext mc, File newFile) throws Exception {
		System.out.println("fileMaker: input: " + inputFile.getAbsolutePath());
		try {
			double a = 10;
			int range = 0;
			int blockLength = 0;
			ArrayList<DataRange> rangeList = null;
			byte[] data = null;
			newFile.createNewFile();
			ByteBuffer buffer = ByteBuffer.allocate(mfr.getBlocksize());
			System.out.println("Reading from file: " + inputFile.getAbsolutePath());
			FileChannel rChannel = new FileInputStream(inputFile).getChannel();
			System.out.println("Writing new file: " + newFile.getAbsolutePath());
			FileChannel wChannel = new FileOutputStream(newFile, true).getChannel();
			System.out.println();
			System.out.print("File completion: ");
			for (int i = 0; i < mc.fileMap.length; i++) {
				mc.fileOffset = mc.fileMap[i];
				System.out.println("get map item: " + i + " - file offset: " + mc.fileOffset);
				if (mc.fileOffset != -1) {
					System.out.println("  read block from local file");
					rChannel.read(buffer, mc.fileOffset);
					buffer.flip();
					wChannel.write(buffer);
					buffer.clear();
				} else {
					System.out.println("   read block from remote file");
					if (!mc.rangeQueue) {
						System.out.println("     range lookup: " + i);
						rangeList = rangeLookUp(i, mfr.getBlocksize(), mc);
						range = rangeList.size();
						data = rangeLoader.get(rangeList);
					} else {
						System.out.println("     already have queued ranges: " + rangeList.size());
					}
					blockLength = calcBlockLength(i, mfr.getBlocksize(), (int) mfr.getLength());
					buffer.put(data, (range - rangeList.size()) * mfr.getBlocksize(), blockLength);
					buffer.flip();
					wChannel.write(buffer);
					buffer.clear();
					rangeList.remove(0);
					if (rangeList.isEmpty()) {
						mc.rangeQueue = false;
					}
				}
				if (log.isTraceEnabled()) {
					if ((((double) i / ((double) mc.fileMap.length - 1)) * 100) >= a) {
						double perc = ((double) i / ((double) mc.fileMap.length - 1)) * 100;
						log.trace("Percent complete: " + perc + "%");
						a += 10;
					}
				}
			}
			log.info("Completed file: " + newFile.getAbsolutePath());
			log.info("Checking checksums...");
			SHA1 sha = new SHA1(newFile);
			if (sha.SHA1sum().equals(mfr.getSha1())) {
				log.info("checksum matches OK");
//				System.out.println("used " + (mfr.getLength() - (mfr.getBlocksize() * missing)) + " " + "local, fetched " + (mfr.getBlocksize() * missing));
//				new File(mfr.getFilename()).renameTo(new File(mfr.getFilename() + ".zs-old"));
//				newFile.renameTo(new File(mfr.getFilename()));
//				allData += mfr.getLengthOfMetafile();
//				System.out.println("really downloaded " + allData);
//				double overhead = ((double) (allData - (mfr.getBlocksize() * missing)) / ((double) (mfr.getBlocksize() * missing))) * 100;
//				System.out.println("overhead: " + df.format(overhead) + "%");
			} else {
				throw new RuntimeException("Checksums don't match");
			}
		} catch (IOException ex) {
			throw new RuntimeException("Can't read or write, check your permissions.");
		}
	}	
	
	/**
	 * Instead of downloading single blocks, we can look into fieMap and collect
	 * amount of missing blocks or end of map accurs. Single ranges are stored in
	 * ArrayList
	 * @param i Offset in fileMap where to start looking
	 * @return ArrayList with ranges for requesting
	 */
	private ArrayList<DataRange> rangeLookUp(int i, int blocksize, MakeContext mc) {
		ArrayList<DataRange> ranges = new ArrayList<DataRange>();
		for (; i < mc.fileMap.length; i++) {
			if (mc.fileMap[i] == -1) {
				ranges.add(new DataRange(i * blocksize,
						(i * blocksize) + blocksize));
			}
			if (ranges.size() >= maxRanges) {
				break;
			}
		}
		if (!ranges.isEmpty()) {
			mc.rangeQueue = true;
		}
		System.out.println("rangeLookup: getting ranges: " + ranges.size());
		return ranges;
	}	
	
	private int calcBlockLength(int i, int blockSize, int length) {
		if ((i * blockSize + blockSize) < length) {
			return blockSize;
		} else {
			return calcBlockLength_b(i, blockSize, length);
		}
	}

	private int calcBlockLength_b(int i, int blockSize, int length) {
		return blockSize + (length - (i * blockSize + blockSize));
	}
	
}
