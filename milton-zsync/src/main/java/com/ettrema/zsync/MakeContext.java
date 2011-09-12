package com.ettrema.zsync;

/**
 * Holds working variables used when applying deltas
 *
 * 
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
	
	
	public void put(int blockIndex, long offset){
		
		fileMap[blockIndex] = offset;
	}
	
	public void delete(ChecksumPair key){
		
		hashtable.delete(key);
	}
	
}
