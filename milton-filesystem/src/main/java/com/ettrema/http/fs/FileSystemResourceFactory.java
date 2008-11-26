package com.ettrema.http.fs;

import com.bradmcevoy.common.Path;
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

    /**
     * Creates and (optionally) initialises the factory. This looks for a 
     * properties file FileSystemResourceFactory.properties in the classpath
     * If one is found it uses the root and realm properties to initialise
     * 
     * If not found the factory is initialised with the defaults
     *   root: user.home system property
     *   realm: milton-fs-test
     * 
     * These initialised values are not final, and may be changed through the 
     * setters or init method
     * 
     * To be honest its pretty naf configuring like this, but i don't  want to
     * force people to use spring or any other particular configuration tool
     * 
     */
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
        log.debug("getResource: host: " + host + " - url:" + url);
        File requested = resolvePath(root,url);
        if( requested == null ) {
            log.debug("file not found");
            return null;
        }
        return resolveFile(requested);
    }

    public String getSupportedLevels() {
        return "1,2";
    }
    
    public FsResource resolveFile(File file) {
        if( !file.exists() ) {
            log.debug("  not found: " + file.getAbsolutePath());
            return null;
        } else if( file.isDirectory() ) {
            log.debug(" found directory: " + file.getAbsolutePath());
            return new FsDirectoryResource(this, file);
        } else {
            log.debug("  found file: " + file.getAbsolutePath());
            return new FsFileResource(this, file);
        }        
    }
    
    public File resolvePath(File root, String url) {
        Path path = Path.path(url);
        File f = root;
        for( String s : path.getParts() ) { 
            f = new File(f,s);
        }
        return f;
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
        this.realm = realm;
    }
}
