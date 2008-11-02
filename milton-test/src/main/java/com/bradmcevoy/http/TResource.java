package com.bradmcevoy.http;

import com.bradmcevoy.http.Request.Method;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

public class TResource implements PostableResource, GetableResource, PropFindableResource, DeletableResource, MoveableResource, CopyableResource {    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResource.class);
    
    String name;
    Date modDate;
    Date createdDate;
    TFolderResource parent;        
    
    private String user;
    private String password;
    
    public TResource(TFolderResource parent, String name) {
        this.parent = parent;
        this.name = name;
        modDate = new Date();
        createdDate = new Date();
        if( parent != null ) {
            parent.children.add(this);
        }
    }

    public void setSecure(String user, String password) {
        this.user = user;
        this.password = password;
    }
    
    public String getHref() {
        if( parent == null ) {
            return "http://localhost:8084/MiltonTestWeb/";
        } else {
            String s = parent.getHref();
            if( !s.endsWith("/") ) s = s + "/";
            s = s + name;
            if( this instanceof CollectionResource ) s = s + "/";
            return s;
        }
    }
    
    
    public void sendContent(OutputStream out, Range range, Map<String, String> params) throws IOException {
        PrintWriter printer = new PrintWriter(out,true);
        sendContentStart(printer);
        sendContentMiddle(printer);
        sendContentFinish(printer);
    }    

    protected  void sendContentMiddle(final PrintWriter printer) {       
        printer.print("rename");
        printer.print("<form method='POST' action='" + this.getHref() + "'><input type='text' name='name' value='" + this.getName() + "'/><input type='submit'></form>");
    }

    protected void sendContentFinish(final PrintWriter printer) {       
        printer.print("</body></html>");
        printer.flush();
    }

    protected void sendContentStart(final PrintWriter printer) {
        printer.print("<html><body>");
        printer.print("<h1>" + getName() + "</h1>");        
        sendContentMenu(printer);        
    }

    protected void sendContentMenu(final PrintWriter printer) {
        printer.print("<ul>");
        for( TResource r : parent.children ) {
            printer.print("<li><a href='" + r.getHref() + "'>" + r.getName() + "</a>");
        }
        printer.print("</ul>");
    }
    

    public Long getContentLength() {
        return null;
    }

    public String getContentType(String accept) {
        return Response.ContentType.HTTP.toString();
    }

    public String checkRedirect(Request request) {
        return null;
    }

    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("processForm: " + parameters.size());
        for( String nm : parameters.keySet() ) {
            log.debug(" - param: " + nm);
        }
        String name = (String)parameters.get("name");
        if( name != null ) {
            this.name = name;
        }
        return null;
    }

    public Long getMaxAgeSeconds() {
        return (long)10;
    }

    public void moveTo(CollectionResource rDest, String name) {
        log.debug("moving..");
        TFolderResource d = (TFolderResource) rDest;
        this.parent.children.remove(this);
        this.parent = d;
        this.parent.children.add(this);
        this.name = name;
    }
        
    public Date getCreateDate() {
        return createdDate;
    }    
    
    public String getName() {
        return name;
    }

    public Object authenticate(String user, String password) {
        if( this.user == null ) return true;
        return  ( user.equals(this.user)) && (password != null && password.equals(this.password));
    }

    public boolean authorise(Request request, Method method, Auth auth) {
        if( auth == null ) {
            if( this.user == null ) {
                return true;
            } else {
                return false;
            }
        } else {
            return ( this.user == null || auth.user.equals(this.user));
        }
    }

    public String getRealm() {
        return "mockRealm";
    }

    public Date getModifiedDate() {
        return modDate;
    }    
    
    public void delete() {
        if( this.parent == null ) throw new RuntimeException("attempt to delete root");
        if( this.parent.children == null ) throw new NullPointerException("children is null");
        this.parent.children.remove(this);
    }
    
    public void copyTo(CollectionResource toCollection, String name) {
        TResource rClone;
        rClone = (TResource) this.clone((TFolderResource)toCollection);
        rClone.name = name;
    }     
    
    protected Object clone(TFolderResource newParent) {
        TResource r = new TResource(newParent,name);
        return r;
    }    

    public int compareTo(Resource o) {
        if( o instanceof TResource ) {
            TResource res = (TResource)o;
            return this.getName().compareTo(res.getName());
        } else {
            return -1;
        }
    }

    public String getUniqueId() {
        return this.hashCode()+"";
    }

}
