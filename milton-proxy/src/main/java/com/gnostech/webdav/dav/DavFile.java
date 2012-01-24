package com.gnostech.webdav.dav;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropPatchableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.bradmcevoy.io.BufferingOutputStream;
import com.ettrema.httpclient.Folder;
import com.ettrema.httpclient.Host;
import com.ettrema.httpclient.HttpException;
import com.ettrema.httpclient.Resource;
import com.gnostech.webdav.logging.DavLogger;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URLEncoder;


public class DavFile extends DavResource implements GetableResource, DeletableResource, MoveableResource, CopyableResource, PropPatchableResource, LockableResource {
	private Logger log = LoggerFactory.getLogger(DavFile.class);
        private DavFolder parent;
    	private String contentType;
	private String etag;
	private String name;
	private String path;
	private long contentlength;
	private Date creation;
	private URI href;
	private Date modified;
        private com.ettrema.httpclient.Folder remoteFile;
        
	ArrayList<DavResource> children = new ArrayList<DavResource>();
	//protected Object clone( DavFolder newParent ) {
        //    return new DavFile( newParent, name, null, -1 );
       // }
	public DavFile(Resource file) {
		this.name = file.name;
                this.path = file.path().toString();
                this.creation = file.getCreatedDate();
                this.modified = file.getModifiedDate();
	}

	public DavFile(DavFolder root, String string, com.googlecode.sardine.DavResource resource,long length, String ct) {
            //modified code to support correct file referencing
            //Commented out the null information below - not needed????
                
            if(resource == null){
                    this.parent = root;
                    this.name = string;
                    //Set to current level
                    //this.path = root.getPath();
                    this.path = string;
                    this.setCreation(new Date());
                    this.setModified(new Date());
                    this.contentlength = length;
                    this.contentType = ct;
                    if(this.getPath()== null){
                try {
                    try {
                        this.setHref(new URI( URLEncoder.encode(this.getName(), "UTF-8")));
                    } catch (UnsupportedEncodingException ex) {
                        java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String reformat = this.getHref().toString();
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    this.setHref(new URI(reformat));
                    
                    //this.setHref(new URI("/" + this.getName()));
                } catch (URISyntaxException ex) {
                    java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                    }else{
                try {
                    try {
                        this.setHref(new URI( URLEncoder.encode(this.getName(), "UTF-8")));
                    } catch (UnsupportedEncodingException ex) {
                        java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String reformat = this.getHref().toString();
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    this.href = new URI(reformat);
                    
                    //this.setHref(new URI(this.path + this.getName().replace(" ", "%20")));
                } catch (URISyntaxException ex) {
                    java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                    }
                }else{
                    this.parent = root;
                    this.name = string;    
                    Map<String,String> customProps = resource.getCustomProps();
                    String Win32CreationTime = (String) customProps.get("Win32CreationTime");
                    String Win32LastModifiedTime = (String) customProps.get("Win32LastModifiedTime");
                    String Win32LastAccessTime = (String) customProps.get("Win32LastAccessTime");
                    //System.out.println(davres.getName() + " " + Win32LastAccessTime);
                    this.setContentType((resource.getContentType()));
                    this.setEtag((resource.getEtag()));
                    this.name = (resource.getName());
                    this.setPath((resource.getName()));
                    this.setContentlength((resource.getContentLength()));
                    if(!(Win32CreationTime == null))
                            this.setCreation(Util.getDate(Win32CreationTime));
                    else
                            this.setCreation(resource.getCreation());
                               

                    String reformat = this.getName();
            try {
                reformat = URLEncoder.encode(reformat, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
            }
                    reformat = name.replace(" ", "%20");
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
            try {
                this.setHref(new URI(reformat));
                //this.setHref((resource.getHref()));
            } catch (URISyntaxException ex) {
                java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
            }
                    
                    this.setModified(Util.getDate(Win32LastAccessTime));
                }
		
        if( parent != null ) {
            //this.user = parent.user;
           // this.password = parent.password;
            checkAndRemove( parent, name );   
            parent.children.add( this );
        }
		
	}
        
    public String getHref(DavFile file) {
            String s = file.getHref().toString();
            DavFolder folder = file.parent;
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
    
    private void checkAndRemove( DavFolder parent, String name ) {
        DavResource r = (DavResource) parent.child( name );
        if( r != null ) parent.children.remove( r );
    }

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void moveTo(CollectionResource rDest, String name)
			throws ConflictException, NotAuthorizedException,
			BadRequestException {
                        String ename = name.replace(" ", "%20");
//            String rdestination = rDest.getName().replace(" ", "%20");
                        DavFolder dfile = (DavFolder)rDest;
        try {
            ename = URLEncoder.encode(ename, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
        }
            String reformat = ename;
            reformat = reformat.replace("%2F","/");
            reformat = reformat.replace("+","%20");
            reformat = reformat.replace("%25","%");
            ename = reformat;
		
		try {
			Sardine sardine = SardineFactory.begin();		
			if(this.parent==null){//if parent is root
				sardine.move(MyResourceFactory.remoteClient + getHref(this), MyResourceFactory.remoteClient + "/" + ename);	
                                log.debug( "moving.." );
        DavLogger.logger.severe(this.getAuthUser() + " MOVE " + this.path + name + " SUCCEEDED - File Moved.");
        DavFolder d = (DavFolder) rDest;
        this.parent.children.remove( this );
        this.parent = d;
        this.parent.children.add( this );
        this.name = name;
                        }else{
				sardine.move(MyResourceFactory.remoteClient + getHref(this), MyResourceFactory.remoteClient + dfile.getHref(dfile) + ename);
                                log.debug( "moving.." );
        DavLogger.logger.severe(this.getAuthUser() + " MOVE " + this.path + name + " SUCCEEDED - File Moved.");
        DavFolder d = (DavFolder) rDest;
        this.parent.children.remove( this );
        this.parent = d;
        this.parent.children.add( this );
        this.name = name;
                        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {     
		}
            

		
	}
    

	@Override
	public void delete() throws NotAuthorizedException, ConflictException,
			BadRequestException {
		try {
			Sardine sardine = SardineFactory.begin("", "");
                        System.out.println(MyResourceFactory.remoteClient + getHref(this));
                        if(sardine.exists(MyResourceFactory.remoteClient + getHref(this))){
			sardine.delete(MyResourceFactory.remoteClient + getHref(this));
                        this.parent.children.remove( this );
                        DavLogger.logger.info(this.getAuthUser() + " DELETE " + this.path + name + " SUCCEEDED - File removed.");
                        }

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		
	}


	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
	
		Sardine sardine = SardineFactory.begin();              
		InputStream is = null;
                //System.out.println("******************** " + MyResourceFactory.remoteClient + this.getHref() + " **************************");
               // if(this.getHref() == null){
               //     is = sardine.get(MyResourceFactory.remoteClient + "/" + this.getName());
               // }
              //  else{
                System.out.println(MyResourceFactory.remoteClient + getHref(this));
                    is = sardine.get(MyResourceFactory.remoteClient + getHref(this));
              //  }
                    
		int numRead;
		byte buf[] = new byte[4096];
		while((numRead = is.read(buf)) != -1){	
                    
			out.write(buf, 0, numRead);
		}
                out.flush();
                is.close();
	}

	@Override
	public void setProperties(Fields arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyTo(CollectionResource arg0, String arg1)
			throws NotAuthorizedException, BadRequestException,
			ConflictException {
		// TODO Auto-generated method stub
		
	}    

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @return the etag
	 */
	public String getEtag() {
		return etag;
	}

	/**
	 * @param etag the etag to set
	 */
	public void setEtag(String etag) {
		this.etag = etag;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @param contentlength the contentlength to set
	 */
	public void setContentlength(long contentlength) {
		this.contentlength = contentlength;
	}


	/**
	 * @param creation the creation to set
	 */
	public void setCreation(Date creation) {
		
		this.creation = creation;
	}

	/**
	 * @return the href
	 */
	public URI getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(URI href) {
		this.href = href;
	}
	/**
	 * @param modified the modified to set
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}

    @Override
    public Date getCreateDate() {
        return creation;
    }

    @Override
    public Date getModifiedDate() {
        return creation;
    }

    @Override
    public String getContentType(String string) {
		return contentType;
    }

    @Override
    public Long getContentLength() {
        return contentlength;
    }

    void setName(String name) {
        this.name = name;
    }
/*
	public DavFile(DavFolder root, String string, com.googlecode.sardine.DavResource resource,long length, String file) throws IOException {
            InputStream tmpfile = null;
        try {
            //modified code to support correct file referencing
            //Commented out the null information below - not needed????
            tmpfile =  new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
        }

            if(resource == null){
                    this.parent = root;
                    this.name = string;
                    //Set to current level
                    //this.path = root.getPath();
                    this.path = string;
                    this.setCreation(new Date());
                    this.setModified(new Date());
                    this.contentlength = length;
                    if(this.getPath()== null){
                try {
                    try {
                        this.setHref(new URI( URLEncoder.encode(this.getName(), "UTF-8")));
                    } catch (UnsupportedEncodingException ex) {
                        java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String reformat = this.getHref().toString();
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    this.setHref(new URI(reformat));
                    
                    //this.setHref(new URI("/" + this.getName()));
                } catch (URISyntaxException ex) {
                    java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                    }else{
                try {
                    try {
                        this.setHref(new URI( URLEncoder.encode(this.getName(), "UTF-8")));
                    } catch (UnsupportedEncodingException ex) {
                        java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    String reformat = this.getHref().toString();
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
                    this.href = new URI(reformat);
                    
                    //this.setHref(new URI(this.path + this.getName().replace(" ", "%20")));
                } catch (URISyntaxException ex) {
                    java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
                }
                    }
                }else{
                    this.parent = root;
                    this.name = string;    
                    Map<String,String> customProps = resource.getCustomProps();
                    String Win32CreationTime = (String) customProps.get("Win32CreationTime");
                    String Win32LastModifiedTime = (String) customProps.get("Win32LastModifiedTime");
                    String Win32LastAccessTime = (String) customProps.get("Win32LastAccessTime");
                    //System.out.println(davres.getName() + " " + Win32LastAccessTime);
                    this.setContentType((resource.getContentType()));
                    this.setEtag((resource.getEtag()));
                    this.name = (resource.getName());
                    this.setPath((resource.getName()));
                    this.setContentlength((resource.getContentLength()));
                    if(!(Win32CreationTime == null))
                            this.setCreation(Util.getDate(Win32CreationTime));
                    else
                            this.setCreation(resource.getCreation());
                               

                    String reformat = this.getName();
            try {
                reformat = URLEncoder.encode(reformat, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
            }
                    reformat = name.replace(" ", "%20");
                    reformat = reformat.replace("%2F","/");
                    reformat = reformat.replace("+","%20");
            try {
                this.setHref(new URI(reformat));
                //this.setHref((resource.getHref()));
            } catch (URISyntaxException ex) {
                java.util.logging.Logger.getLogger(DavFile.class.getName()).log(Level.SEVERE, null, ex);
            }
                    
                    this.setModified(Util.getDate(Win32LastAccessTime));
                }
		
        if( parent != null ) {
            //this.user = parent.user;
           // this.password = parent.password;
            checkAndRemove( parent, name );   
            parent.children.add( this );
        }
        
		
	}
        
*/
}
