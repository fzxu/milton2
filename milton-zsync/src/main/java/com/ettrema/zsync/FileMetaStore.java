package com.ettrema.zsync;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.io.StreamUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Very simple implementation which uses files in a temp dir
 *
 * @author brad
 */
public class FileMetaStore implements MetaStore {

	private File tempDir;

	public FileMetaStore(File tempDir) {
		this.tempDir = tempDir;
	}
		
	
	public File storeMetaData(Resource r, InputStream in) throws Exception {
		File metaFile = new File(tempDir, r.getUniqueId());
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(metaFile);
			StreamUtils.readTo(in, fout);
			return metaFile;
		} finally {
			StreamUtils.close(fout);
		}
	}

	public File getMetaData(Resource r) throws Exception {
		File metaFile = new File(tempDir, r.getUniqueId());
		if( metaFile.exists() ) {
			return metaFile;
		} else {
			return null;
		}
	}
}
