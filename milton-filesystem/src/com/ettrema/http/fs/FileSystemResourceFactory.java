package com.ettrema.http.fs;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
        Properties props = new Properties();
        InputStream in = FileSystemResourceFactory.class.getResourceAsStream("FileSystemResourceFactory.properties");
        if( in != null ) {
            log.debug("Configuring from FileSystemResourceFactory.properties");
            try {
                props.load(in);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            String sRoot = props.getProperty("root");
            log.debug("root: " + sRoot);
            String sRealm = props.getProperty("realm");
            init(sRoot, sRealm);
        } else {
            log.debug("Configuring from defaults");
            String sRoot = System.getProperty("user.home");
            String sRealm = "milton-fs-test";
            init(sRoot, sRealm);
        }
    }
    
    protected void init(String sRoot, String sRealm) {
        log.debug("root: " + sRoot + " - realm:" + sRealm);
        setRoot( new File(sRoot));        
        setRealm(sRealm);
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
    
    public FsResource resolveFile(File file) {
        if( !file.exists() ) {
            return null;
        } else if( file.isDirectory() ) {
            return new FsDirectoryResource(this, file);
        } else {
            return new FsFileResource(this, file);
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
        return 60l*60;
    }
    
    

    private void setRealm(String realm) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
