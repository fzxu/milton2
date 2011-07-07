package com.ettrema.zsync;

/**
 * Holds working variables used when applying deltas
 *
 * @author brad
 */
public class MakeContext {
	final ChainingHash hashtable;
	final long[] fileMap;
	long fileOffset;
	boolean rangeQueue;

	public MakeContext(ChainingHash hashtable, long[] fileMap) {
		this.hashtable = hashtable;
		this.fileMap = fileMap;
	}
	
}
