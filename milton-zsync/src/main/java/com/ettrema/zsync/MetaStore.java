package com.ettrema.zsync;

import com.bradmcevoy.http.Resource;
import java.io.File;
import java.io.InputStream;

/**
 * An interface to permit holding metadata which has been POST'ed, but not yet
 * used in a PUT.
 * 
 * The POST and PUT must be atomic so we can assume that there may only ever
 * be at most one applicable set of metadata per resource
 *
 * @author brad
 */
public interface MetaStore {
	/**
	 * Store the given metadata and return a file to access it
	 * 
	 * @param r
	 * @param in
	 * @return 
	 */
	File storeMetaData(Resource r, InputStream in) throws Exception;
	
	
	/**
	 * Get previously stored metadata for a resource
	 * 
	 * @param r
	 * @return 
	 */
	File getMetaData(Resource r) throws Exception;
}
