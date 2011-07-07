package jazsync.jazsync;

import com.ettrema.http.DataRange;
import java.util.List;

/**
 *
 * @author brad
 */
public interface RangeLoader {

	public byte[] get(List<DataRange> rangeList) throws Exception;
	
}
