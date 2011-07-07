package com.ettrema.zsync;

import com.ettrema.http.DataRange;
import java.util.List;

/**
 *
 * @author brad
 */
public interface RangeLoader {

	public byte[] get(List<DataRange> rangeList) throws Exception;
	
}
