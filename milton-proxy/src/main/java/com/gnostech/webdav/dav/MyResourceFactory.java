/**
 *
 *
 * @author Octavio Gutierrez
 */
package com.gnostech.webdav.dav;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.ettrema.httpclient.Folder;
import com.ettrema.httpclient.Host;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


    public class MyResourceFactory implements ResourceFactory {

        //public static Host rc = new Host("192.168.1.114",8010, "", "", null);
        public static final DavFolder ROOT = new DavFolder((DavFolder)null,"");
        //public static final DavFolder ROOT = new DavFolder(rc);
        public static final String remoteClient = "http://192.168.1.114:8010";
        //public static final String remoteClient = readFirstLineOfFile("c:\\ip.txt");

        private Logger log = LoggerFactory.getLogger(MyResourceFactory.class );	
       /*
    public Resource getResource(String host, String url) {
        log.debug("getResource: url: " + url );
        Path path = Path.path(url);
        Resource r = find(path); // recursive call
        log.debug("_found: " + r);   
        return r;
    }                
               
    private DavResource find(Path path) {
        if( isRoot(path) ) return ROOT; // statically defined root folder
        DavResource r = find(path.getParent()); // recurse up the path to get a reference to parent
        if( r == null ) return null; // didnt find a parent, so definitely won't find child
        if( r instanceof DavFolder ) { // found a parent folder, so look for child name from path.getName()
            DavFolder folder = (DavFolder)r;
            DavResource r2 = (DavResource)folder.child(path.getName());
            if( r2 != null ) {
                return r2;
            }
        }
        log.debug("not found: " + path); // either didnt find parent, or the specified child
        return null;
    }
    
    private boolean isRoot( Path path ) {
	if( path == null ) return true;
	return ( path.getParent() == null || path.getParent().isRoot());
	}               
                
	*/
        static {    
                       
                        recursePath(ROOT,null);
 
                }

        
	public static Resource recursePath(DavFolder parent, String pres) {
		String ename = null;
		if(pres!= null){
            try {
                //	ename = pres.replace(" ", "%20");
                    ename = URLEncoder.encode(pres, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(MyResourceFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
                    String reformat = ename;
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    reformat = reformat.replace("%25","%");
                    ename = reformat;
		}
		//String urlSafe = ename.replace(" ", "+");
	    
		Sardine sardine = SardineFactory.begin();
		@SuppressWarnings("deprecation")
		List<com.googlecode.sardine.DavResource> resources = null;
		try {
			if(pres==null){
				resources = sardine.getResources(remoteClient);
			}else{
				resources = sardine.getResources(remoteClient + ename);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (com.googlecode.sardine.DavResource res : resources)
		{
			if((!(res.toString().equals("/")))&& res.isDirectory() && !res.getPath().equals(pres)){
				DavFolder ds = new DavFolder(parent,res.getName());
				recursePath(ds,res.getPath());
				System.out.println("\t\t"+res.getPath());
			}
			
			if(!res.isDirectory()){
			    DavFile fileds = new DavFile(parent,res.getName(),res, -1,"");
				//recursePath(folder, res.getPath());
				System.out.println(res.getPath());
			}
			
			
		}
		return null;
	}
	
	  public Resource getResource(String host, String url) {
              File directory = new File("c:\\tmp\\");
              boolean exists = directory.exists();
              if(!exists){
                  directory.mkdir();
              }
	        //log.debug("getResource: url: " + url );
	        Path path = Path.path(url);
		
		//STRIP PRECEEDING PATH
		//path = path.getStripFirst();
	        Resource r = find(path); // recursive call
	        //log.debug("_found: " + r);
	        return r;
	    }

	    private DavResource find(Path path) {
	        if( path.isRoot() ) return ROOT; // statically defined root folder
	        DavResource r = find(path.getParent()); // recurse up the path to get a reference to parent
	        if( r == null ) return null; // didnt find a parent, so definitely won't find child
	        if( r instanceof DavFolder ) { // found a parent folder, so look for child name from path.getName()
	        	    DavFolder folder = (DavFolder)r;
	                    for( Resource rChild : folder.getChildren() ) {
	                    DavResource r2 = (DavResource) rChild;
	                    if( r2.getName().equals(path.getName())) {
	                        return r2;
	                    } else {
//	                        log.debug( "IS NOT: " + r2.getName() + " - " + path.getName());
	                    }
	                }
	            }
	        if(path != null){
	        //    log.debug("not found: " + path);
	        }
	            return null;
	    }
	    
	    private boolean isRoot( Path path ) {
	        if( path == null ) return true;
	        return ( path.getParent() == null || path.getParent().isRoot());
	    }

       private static String readFirstLineOfFile(String fn){
        String lineData = "";

        try{
            RandomAccessFile inFile = new RandomAccessFile(fn,"rw");
            lineData = inFile.readLine();
            inFile.close();
        }//try
        catch(IOException ex){
            System.err.println(ex.getMessage());
        }//catch

        return lineData;

    }


}

