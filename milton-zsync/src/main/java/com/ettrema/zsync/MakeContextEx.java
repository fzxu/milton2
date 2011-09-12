package com.ettrema.zsync;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * An extension of MakeContext that stores block matches in a way more suitable for upload.<p/>
 * 
 * This object differs from a MakeContext in that it allows multiple local blocks to be matched to a single remote block.
 * It does this by overriding the <code>put</code> method to save matches to a List of OffsetPairs, rather than 
 * to <code>fileMap</code>, which only allows one entry per remote block. It also overrides the <code>delete</code> 
 * method to do nothing, instead of removing the ChecksumPair from the hashtable.<p/>
 * 
 * MakeContextEx is used internally by UploadMakerEx in place of MakeContext as an argument to MapMatcher.mapMatcher.<p/>
 * 
 * 
 * @see UploadMakerEx
 * @author Nick
 *
 */
public class MakeContextEx extends MakeContext {
	
	private List<OffsetPair> reverseMap;
	
	/**
	 * Constructs a MakeContextEx from an already-initialized ChainingHash and a long array.
	 * 
	 * @param hashtable The hashtable obtained from a MetaFileReader
	 * @param fileMap An array whose size is the block count and whose values should be initialized to -1 
	 */
	public MakeContextEx(ChainingHash hashtable, long[] fileMap) {
		super(hashtable, fileMap);
		reverseMap = new ArrayList<OffsetPair>();
		
	}
	
	/**
	 * Returns a list of OffsetPairs representing the block-matches between the local and
	 * remote file.
	 * 
	 * @return The list of block matches
	 * @see OffsetPair
	 */
	public List<OffsetPair> getReverseMap() {
		return reverseMap;
		
	}
	
	/**
	 * Adds a match to the list of block matches.<p/> 
	 * 
	 * Note: For compatibility with MapMatcher (specifically, the <code>MapMatcher.matchControl</code> 
	 * method), this uses the somewhat messy solution of storing matches in both the reverseMap 
	 * and the fileMap.
	 * 
	 * @param blockIndex Index of the remote block
	 * @param offset Byte offset of the local block
	 */
	@Override
	public void put(int blockIndex, long offset){
		
		if (blockIndex == super.fileMap.length){
			
			return;
		}
		reverseMap.add(new OffsetPair(offset, blockIndex));
		super.fileMap[blockIndex] = offset;
	}
	
	/**
	 * Overrides <code>delete</code> in MakeContext to do nothing. Local blocks
	 * with the identical checksums will all then match to the first matching ChecksumPair in ChainingHash
	 * 
	 * @param key Unused in this override. Can be null
	 */
	@Override
	public void delete(ChecksumPair key){
		return;
	}



}

/**
 * An object representing a single match between a block on the client file and a block
 * on the server file. The Pair (localOffset, remoteBlock) means that the block in the local file
 * at byte <code>localOffset</code> is identical to block number <code>remoteBlock</code> in
 * the remote file.
 * 
 * @author Nick
 */
class OffsetPair{
	
	/**
	 * The byte offset of the block in the local file 
	 */
	public final long localOffset;
	/**
	 * The index of the block in the remote file
	 */
	public final long remoteBlock;

	
	/**
	 * Constructs an immutable OffsetPair. 
	 * 
	 * @param offset The start byte of the local block
	 * @param blockIndex The index of the remote block
	 */
	public OffsetPair(long offset, long blockIndex){
		
		localOffset = offset;
		remoteBlock = blockIndex;
	}

	/**
	 * A Comparator used to sort a list of OffsetPairs by their remoteBlock values.
	 * 
	 * @author Nick
	 *
	 */
	static class RemoteSort implements Comparator<OffsetPair>{

		@Override
		public int compare(OffsetPair o1, OffsetPair o2) {
			
			return (int) (o1.remoteBlock - o2.remoteBlock);
		}
		
	}

	/**
	 * A Comparator used to sort a list of OffsetPairs by their localOffset values.
	 * 
	 * @author Nick
	 *
	 */
	static class LocalSort implements Comparator<OffsetPair>{

		@Override
		public int compare(OffsetPair o1, OffsetPair o2) {
			
			return (int) (o1.localOffset - o2.localOffset);
		}
		
		
	}
	
}
