package jazsync.jazsync;

import java.util.ArrayList;

/**
 *
 * @author brad
 */
public interface RangeLoader {

	public byte[] get(ArrayList<DataRange> rangeList);
	
}
