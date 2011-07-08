package com.ettrema.zsync;

import com.bradmcevoy.http.Range;
import java.util.List;

/**
 *
 * @author brad
 */
public interface RangeLoader {

	public byte[] get(List<Range> rangeList) throws Exception;
	
}
