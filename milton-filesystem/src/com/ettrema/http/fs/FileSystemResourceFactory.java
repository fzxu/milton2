package com.ettrema.http.fs;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FileSystemResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(FileSystemResourceFactory.class);
    
    File root;
    String realm;

    public FileSystemResourceFactory() {
    }

    public FileSystemResourceFactory(File root, String realm) {
        setRoot(root);
        setRealm(realm);
    }

    public File getRoot() {
        return root;
    }

    public void setRoot(File root) {
        this.root = root;
        if( !root.exists() ) {
            log.warn("Root folder does not exisst: " + root.getAbsolutePath());
        }
        if( !root.isDirectory() ) {
            log.warn("Root exists but is not a directory: " + root.getAbsolutePath());
        }
    }
        
    
    public Resource getResource(String host, String url) {
        File requested = resolvePath(root,url);
        return resolveFile(requested);
    }

    public String getSupportedLevels() {
        return "1,2";
    }
    
    public static FsResource resolveFile(File file) {
        if( !file.exists() ) {
            return null;
        } else if( file.isDirectory() ) {
            return new FsDirectoryResource(file);
        } else {
            return new FsFileResource(file);
        }        
    }
    
    public File resolvePath(File root, String url) {
        // todo
        return null;
    }

    public String getRealm() {
        return realm;
    }

    /**
     * 
     * @return - the caching time for files
     */
    public Long getMaxAgeSeconds() {
        return 60*60;
    }
    
    

    private void setRealm(String realm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
