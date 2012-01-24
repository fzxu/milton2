package com.gnostech.webdav.dav;


import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.*;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.httpclient.Folder;
import com.gnostech.webdav.logging.DavLogger;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import java.net.URI;
import java.net.URISyntaxException;


public class DavFolder extends DavResource implements CollectionResource, PutableResource, MakeCollectionableResource, DeletableResource, MoveableResource, LockingCollectionResource, CopyableResource, PropPatchableResource {

	private Logger log = LoggerFactory.getLogger(DavFolder.class);
    	private String contentType;
	private String etag;
	private String name;
	private String path;
	private long contentlength;
	private Date creation;
	private URI href;
	private Date modified;
	private String fullpath;
	private String absolute;
        private String firsthref;
        private String secondhref;
        Sardine sardine = SardineFactory.begin();
        DavFolder parent;
        private com.ettrema.httpclient.Folder remoteFolder;
        
	//private String parent = null;
        
	ArrayList<DavResource> children = new ArrayList<DavResource>();
	
	public DavFolder(com.ettrema.httpclient.Resource folder) {
		this.name = folder.name;
                this.path = folder.path().toString();
                remoteFolder = (Folder)folder;
        try {
            this.href = new URI(folder.href());
        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }

	}

	//public DavFolder(DavFolder parent, String name, com.googlecode.sardine.DavResource resource) {
        public DavFolder(DavFolder parent, String name) {
            
            this.parent = parent;
            if(name.equals("")){
                this.name = null;
                this.path = "/";
            try {
                this.href = new URI("/");
            } catch (URISyntaxException ex) {
                java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            this.name = name;
            this.path = name + "/";
            this.href = Util.formatURL(this.path);
        }

            if( parent != null ) {
            checkAndRemove( parent, name );
            parent.children.add( this );
        }
	}
    
    public DavFolder(DavFolder parent, String name, com.googlecode.sardine.DavResource davResource) {
 
            this.parent = parent;
            if(name.equals("")){
                this.name = null;
                this.path = "/";
            try {
                this.href = new URI("/");
            } catch (URISyntaxException ex) {
                java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            this.name = name;
            this.path = this.parent.path + name + "/";
            this.href = Util.formatURL(this.path);
        }

            if( parent != null ) {
                checkAndRemove( parent, name );
                parent.children.add( this );
        }
	}

    private void checkAndRemove( DavFolder parent, String name ) {
        DavResource r = (DavResource) parent.child( name );
        if( r != null ) parent.children.remove( r );
    }
	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return creation;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
        public String getPath(){
            return path;
        }

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return creation;
	}

	@Override
	public void moveTo(CollectionResource rDest, String name)
			throws ConflictException, NotAuthorizedException,
			BadRequestException {
        DavFolder omg = (DavFolder)rDest;
        String ename = Util.formatURL(name).toString();
	try {
            Sardine sardine = SardineFactory.begin();	
            if(this.parent.path == "/"){//if parent is root
                sardine.move(MyResourceFactory.remoteClient + getHref(this), MyResourceFactory.remoteClient + getHref(omg) + ename);	
                log.debug( "moving.." );       
                DavFolder d = (DavFolder) rDest;
                String original = getHref(this);
                this.parent.children.remove( this );
                this.parent = d;
                this.parent.children.add( this );
                this.name = name;
                this.path = d.href + name + "/";
                this.href = Util.formatURL(this.path);
                DavLogger.logger.log(Level.INFO, "{0} MOVE FROM: {1} TO: {2} SUCCEEDED - Folder moved", new Object[]{this.getAuthUser(), original, getHref(this,null)});
                }
                else{
                sardine.move(MyResourceFactory.remoteClient + getHref(this), MyResourceFactory.remoteClient  + getHref(omg) + ename);

                log.debug( "moving.." );       
                DavFolder d = (DavFolder) rDest;
                String original = getHref(this);
                this.parent.children.remove( this );
                this.parent = d;
                this.parent.children.add( this );
                this.name = name;
                this.path = name + "/";
                this.href = Util.formatURL(this.path);
                DavLogger.logger.log(Level.INFO, "{0} MOVE FROM: {1} TO: {2} SUCCEEDED - Folder moved", new Object[]{this.getAuthUser(), original, getHref(this,null)});
                }
                } catch (Exception e) {
                        e.printStackTrace();
                } finally {}

	}

	@Override
	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
            		try {                            
			Sardine sardine = SardineFactory.begin("", "");
                            sardine.delete(MyResourceFactory.remoteClient + getHref(this));
                                    if( this.parent == null )
            throw new RuntimeException( "attempt to delete root" );

        if( this.parent.children == null )
            throw new NullPointerException( "children is null" );
        this.parent.children.remove( this );
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//Perform cleanup of connections - Sardine handles it's connections internally in it's API
		}
                        

		
	}

	@Override
	public CollectionResource createCollection(String newName)
			throws NotAuthorizedException, ConflictException,
			BadRequestException {

        Sardine sardine = SardineFactory.begin();
        String newEncodedName = newName;
        newEncodedName = Util.formatURL(newName).toString();

        try {
            if(this.parent == null){
                sardine.createDirectory(MyResourceFactory.remoteClient + "/" + newEncodedName);
            }else{
                sardine.createDirectory(MyResourceFactory.remoteClient +  getHref(this) + newEncodedName);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        log.debug( "createCollection: " + newName);
        DavFolder r = new DavFolder(this,newName);
        return r;
	}

   public com.bradmcevoy.http.Resource createNew(String name, InputStream is, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {

                log.debug(this.getAuthUser() + " attempting to create a new file in this folder name={} length={} contentType={}",new Object[]{name,length,contentType});
		String ename = name;
	    	ename =  URLEncoder.encode(ename, "UTF-8");	
                String reformat = ename;
                reformat = reformat.replace("+","%20");
                ename = reformat;
                File dest = new File("c:\\tmp\\" + name);

                if(length > 0){
		
		FileOutputStream out = null;
                
		try {
			out = new FileOutputStream(dest);
			IOUtils.copy(is, out);
		} catch(Exception e){
                    System.out.println(e.getMessage());
                }finally {
			//IOUtils.closeQuietly(out);
                    out.close();
		}
                }else{
                    dest.createNewFile();
                }
                InputStream tmpfile =  new FileInputStream(dest.getAbsoluteFile());
                

                
                String remoteClient = MyResourceFactory.remoteClient.replace(":8010", "");
                remoteClient = remoteClient.replace("http://", "");
              //  if(this.parent == null){
/*
                         Host host = new Host(remoteClient,8010,"","",null);
                         Folder rootFolder = host;
            try {
                rootFolder.upload(dest);
            } catch (HttpException ex) {
                java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                        }else{
                            
                        Host host = new Host(remoteClient,8010,"","",null);
                        String ipath = getHref(this,null);
			Folder rootFolder = host;
                        String[] split = ipath.split("/");
                        try{
                            com.ettrema.httpclient.Resource child = rootFolder.child(split[0]);
                            Folder childFolder = null;
                            if(child instanceof Folder){
                                childFolder = (Folder)child;
                                for(int x = 2; x < split.length; x++){
                                if(split[x]!=null){
                                    Folder subFolder = (Folder)childFolder.child(split[x]);
                                    childFolder = subFolder;
                                }
                                
                            }
                                try{
                                childFolder.uploadFile(dest);
                                }catch(Exception e){
                                    System.out.println("blah blah blah");
                                }
                            }
                        }catch(HttpException e){
                            e.printStackTrace();
                        }
                }

                
   */             

                                
          Sardine sardine = SardineFactory.begin();
          DavFile fileds = null;
        try {
            
            //String lockSardine = sardine.lock(MyResourceFactory.remoteClient+ getHref(this)+name); 
            System.out.println(MyResourceFactory.remoteClient+ getHref(this) + name + " ---------");
            sardine.enableCompression();
            sardine.put(MyResourceFactory.remoteClient+ getHref(this) + Util.formatURL(name),tmpfile);
            //sardine.unlock(MyResourceFactory.remoteClient+ getHref(this)+name, lockSardine);
            fileds = new DavFile(this,name,null,length, contentType);
            if(length > 0){
            DavLogger.logger.info(this.getAuthUser() + " PUT " + getHref(this) + name + " SUCCEEDED - File passed policy");
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }

                                /* 
                        dest.delete();
                        while(dest.exists()){
			dest.delete();
		
			}
*/
                //System.out.println(MyResourceFactory.remoteClient+ getHref(this)+name);
                String lockSardine = null;
                boolean done = false;
                //if(lock != null){
                //lockSardine = sardine.lock(MyResourceFactory.remoteClient+ getHref(this)+name);     
                sardine.enableCompression();
                 
               // done = true;
              //  }
               // sardine.put(MyResourceFactory.remoteClient+ getHref(this)+name, is);        
               // if(done == true){
               // sardine.unlock(MyResourceFactory.remoteClient+ getHref(this)+name, lockSardine);
                //done = false;
               // }
		return  fileds;
		}

	@Override
	public Resource child(String childName) {
  
        for( Resource r : getChildren() ) {
            if( r.getName().equals(childName)) return r;
        }
        
                return null;
        /*
        com.ettrema.httpclient.Resource r = null;
        try {
            r = remoteFolder.child(childName);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HttpException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }
            if(r == null){
                return null; // 404 not found
            }else{
                if(r instanceof com.ettrema.httpclient.Folder){
                    return new DavFolder((com.ettrema.httpclient.Folder)r);
                }else{
                    return new DavFile((com.ettrema.httpclient.File)r);
                }
            }
             * 
             */
        } 

	

	@Override
	public List<? extends Resource> getChildren() {
		return children;
	}

    private void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private void setEtag(String etag) {
        this.etag = etag;
    }

    private void setPath(String path) {
        this.path = path;
    }

    private void setContentlength(Long contentlength) {
        this.contentlength = contentlength;
    }

    private void setCreation(Date creation) {
        this.creation = creation;
    }

    private void setHref(URI href) {
        this.href = href;
    }

    private void setModified(Date modified) {
        this.modified = modified;
    }

    public LockToken createAndLock(String name, LockTimeout timeout, LockInfo lockInfo) {
        DavFile temp = new DavFile(this, name, null, -1, "");
        LockResult r = temp.lock(timeout, lockInfo);
        if( r.isSuccessful() ) {
            System.out.println("%%%%%%%%%%%%%%%%%%%%%% Sucessfully Locked %%%%%%%%%%%%%%%%%%%%%%");
            return r.getLockToken();
        } else {
            throw new RuntimeException("didnt lock");
        }
    }

    public URI getHref() {
        return this.href;
    }
    
    public String getHref(DavFolder folder) {
            String s = null;
            while(!folder.getPath().equals("/")){
                
                if(s!=null){
                    s = folder.getHref().toString() + s;
                }else{
                    s = folder.getHref().toString();
                }
                folder = folder.parent;
                
            }
            if(s == null){
                return "/";
            }else{
                return ("/" + s);
            }
    }

        public String getHref(DavFolder folder, String cr) {
            String s = null;
            while(!folder.getPath().equals("/")){
                
                if(s!=null){
                    s = folder.getPath().toString() + s;
                }else{
                    s = folder.getPath().toString();
                }
                folder = folder.parent;
                
            }
            if(s == null || s.equals("/")){
                return "/";
            }else{
                return (s);
            }
    }
    
    @Override
    public void copyTo(CollectionResource cr, String string) throws NotAuthorizedException, BadRequestException, ConflictException {
            DavFolder omg = (DavFolder)cr;
            String ename = string.replace(" ", "%20");
//            String rdestination = rDest.getName().replace(" ", "%20");
        try {
            
            ename = URLEncoder.encode(ename, "UTF-8");
            String reformat = ename;
            reformat = reformat.replace("%2F","/");
            reformat = reformat.replace("+","%20");
            reformat = reformat.replace("%25","%");
            ename = reformat;
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }
		
		try {
			Sardine sardine = SardineFactory.begin();	
                        
			if(this.parent.path == "/"){//if parent is root
                                
				sardine.copy(MyResourceFactory.remoteClient + getHref(this), MyResourceFactory.remoteClient + getHref(omg) + ename);	
        log.debug( "copying.." );       
        DavFolder d = (DavFolder) cr;
        //this.parent.children.remove( this );
        //this.parent = d;
        this.parent.children.add( d );
        d.name = string;
        d.path = string + "/";
        try {
            try {
                    d.href = new URI( URLEncoder.encode(d.path, "UTF-8"));
                    String reformat = d.href.toString();
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    reformat = reformat.replace("%25","%");
                    d.href = new URI(reformat);
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
                }
            //this.href = new URI(this.path);
        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }
                        }
                        else{
                          
				sardine.copy(MyResourceFactory.remoteClient + getHref(this), MyResourceFactory.remoteClient  + getHref(omg) + ename);
        
        log.debug( "copying.." );       
        DavFolder d = (DavFolder) cr;
        //this.parent.children.remove( this );
        //this.parent = d;
        this.parent.children.add( d );
        d.name = string;
        d.path = string + "/";
        try {
            try {
                    d.href = new URI( URLEncoder.encode(d.path, "UTF-8"));
                    String reformat = d.href.toString();
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    reformat = reformat.replace("%25","%");
                    reformat = reformat.replace("%28","(");
                    reformat = reformat.replace("%29",")");
                    
                    d.href = new URI(reformat);
                } catch (UnsupportedEncodingException ex) {
                    java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
                }
            //this.href = new URI(this.path);
        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(DavFolder.class.getName()).log(Level.SEVERE, null, ex);
        }
                        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {     
		}
	
    }
    
    	//protected Object clone( DavFolder newParent ) {
     //       return new DavFile( newParent, name, null, -1 );
     //   }
        
   //     @Override
  //  protected Object clone() throws CloneNotSupportedException {
  //      DavFolder r = new DavFolder(parent,name);
  //      for( DavResource child : children ) {
   //         DavFile df = (DavFile)child;
   //         df.clone(r); // cstr adds to children
     //   }
    //    return r;
   // }

    @Override
    public void setProperties(Fields fields) {
    }

 
  private String readFirstLineOfFile(String fn){
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
