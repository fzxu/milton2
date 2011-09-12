package com.ettrema.zsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.bradmcevoy.http.Range;

/**
 * Tests for both UploadMaker and UploadMakerEx classes
 * 
 * @author Nick
 *
 */
public class UploadMakerTests {
	
	long[] fileMap = {-1, -1, 20, 90, 100, 110, -1, 70, 80, -1};
	long fileLength = 200;
	int blockSize = 10;
	
	@Test
	public void testServersMissingRanges() {
		
		List<Range> expRanges = new ArrayList<Range>();
		expRanges.add(new Range(0, 20));
		expRanges.add(new Range(30, 70));
		expRanges.add(new Range(120, 200));
		
		List<Range> actRanges = UploadMaker.serversMissingRanges(fileMap,
				fileLength, blockSize);
		
		
		String expString = "", actString = "";
		for (Range expRange: expRanges){
			expString += expRange.getRange() + " ";
		}
		for (Range actRange: actRanges){
			actString += actRange.getRange() + " ";
		}
		
		Assert.assertEquals( expString, actString );
	}
	
	@Test
	public void testServersRelocationRanges() {
		
		List<RelocateRange> expRelocs = new ArrayList<RelocateRange>();
		expRelocs.add(new RelocateRange(new Range(3, 6), 90));
		
		List<RelocateRange> actRelocs = UploadMaker.serversRelocationRanges(fileMap, 
				blockSize, fileLength, true);
		
		String expString = Arrays.toString( expRelocs.toArray() );
		String actString = Arrays.toString( actRelocs.toArray() );
		
		Assert.assertEquals( expString , actString );
		
	}
	
	@Test
	public void testServersMissingRangesEx() {
		
		List<OffsetPair> reverseMap = new ArrayList<OffsetPair>();
		reverseMap.add(new OffsetPair(20, 2 ));
		reverseMap.add(new OffsetPair(90, 3 ));
		reverseMap.add(new OffsetPair(100, 4 ));
		reverseMap.add(new OffsetPair(110, 5 ));
		reverseMap.add(new OffsetPair(70, 7 ));
		reverseMap.add(new OffsetPair(80, 8 ));
		
		List<Range> expRanges = new ArrayList<Range>();
		expRanges.add(new Range(0, 20));
		expRanges.add(new Range(30, 70));
		expRanges.add(new Range(120, 200));
		
		List<Range> actRanges = UploadMakerEx.serversMissingRangesEx(reverseMap, 
				fileLength, blockSize);
		
		String expString = "", actString = "";
		for (Range expRange: expRanges){
			expString += expRange.getRange() + " ";
		}
		for (Range actRange: actRanges){
			actString += actRange.getRange() + " ";
		}
		
		Assert.assertEquals( expString, actString );
		
	}
	
	@Test
	public void testServersRelocationRangesEx() {
		
		List<OffsetPair> reverseMap = new ArrayList<OffsetPair>();
		reverseMap.add(new OffsetPair(20, 2 ));
		reverseMap.add(new OffsetPair(90, 3 ));
		reverseMap.add(new OffsetPair(100, 4 ));
		reverseMap.add(new OffsetPair(110, 5 ));
		reverseMap.add(new OffsetPair(70, 7 ));
		reverseMap.add(new OffsetPair(80, 8 ));
		
		List<RelocateRange> expRelocs = new ArrayList<RelocateRange>();
		expRelocs.add(new RelocateRange(new Range(3, 6), 90));
		
		List<RelocateRange> actRelocs = UploadMakerEx.serversRelocationRangesEx(reverseMap,
				blockSize, fileLength, true);
		
		String expString = Arrays.toString( expRelocs.toArray() );
		String actString = Arrays.toString( actRelocs.toArray() );
		
		Assert.assertEquals( expString , actString );
	}

}
