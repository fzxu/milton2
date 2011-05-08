package com.ettrema.httpclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import com.ettrema.httpclient.PropFindMethod.Response;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Folder extends Resource {

    private static final Logger log = LoggerFactory.getLogger(Folder.class);
    private boolean childrenLoaded = false;
    private final List<Resource> children = new CopyOnWriteArrayList<Resource>();
    final List<FolderListener> folderListeners = new ArrayList<FolderListener>();

    /**
     *  Special constructor for Host
     */
    Folder() {
        super();
    }

    public Folder(Folder parent, Response resp) {
        super(parent, resp);
    }

    public Folder(Folder parent, String name) {
        super(parent, name);
    }

    public void addListener(FolderListener l) throws IOException, HttpException {
        for (Resource r : this.children()) {
            l.onChildAdded(r.parent, r);
        }
        folderListeners.add(l);
    }

    public String post(String relativePath, Map<String, String> params) throws HttpException {
        return host().doPost(href() + relativePath, params);
    }

    public File downloadTo(File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException {
        File thisDir = new File(destFolder, this.name);
        thisDir.mkdir();
        for (Resource r : this.children()) {
            r.downloadTo(thisDir, listener);
        }
        return thisDir;
    }

    public void flush() throws IOException {
        if (children != null) {
            log.trace("flush: " + this.name);
            for (Resource r : children) {
                notifyOnChildRemoved(r);
            }
            children.clear();
            childrenLoaded = false;
        }
//        children();
    }

    public List<? extends Resource> children() throws IOException, HttpException {
        if (childrenLoaded) {
            return children;
        }

        String href = href();
        if (log.isTraceEnabled()) {
            log.trace("load children for: " + href);
        }
        List<Response> responses = host().doPropFind(href(), 1);
        childrenLoaded = true;
        if (responses != null) {
            for (Response resp : responses) {
                if (!resp.href.equals(this.href())) {
                    try {
                        Resource r = Resource.fromResponse(this, resp);
                        if (!r.href().equals(this.href())) {
                            children.add(r);
                        }
                        this.notifyOnChildAdded(r);
                    } catch (Exception e) {
                        log.error("couldnt process record", e);
                    }
                }
            }
        } else {
            log.trace("null responses");
        }
        return children;
    }

    public void removeListener(FolderListener folderListener) {
        this.folderListeners.remove(folderListener);
    }

    @Override
    public String toString() {
        return href() + " (is a folder)";
    }

    public void upload(File f) throws IOException, HttpException {
        upload(f, null, null);
    }

    /**
     *
     * @param f
     * @param listener
     * @param throttle - optional, can be used to slow down the transfer
     * @throws IOException
     */
    public void upload(File f, ProgressListener listener, Throttle throttle) throws IOException, HttpException {
        if (f.isDirectory()) {
            uploadFolder(f, listener, throttle);
        } else {
            uploadFile(f, listener, throttle);
        }
    }

    protected void uploadFile(File f, ProgressListener listener, Throttle throttle) throws FileNotFoundException, IOException, HttpException {
        NotifyingFileInputStream in = null;
        try {
            in = new NotifyingFileInputStream(f, listener, throttle);
            upload(f.getName(), in, f.length());
            flush();
        } finally {
            Utils.close(in);
            listener.onComplete(f.getName());
        }
    }

    protected void uploadFolder(File folder, ProgressListener listener, Throttle throttle) throws IOException, HttpException {
        if (folder.getName().startsWith(".")) {
            return;
        }
        Folder newFolder = createFolder(folder.getName());
        for (File f : folder.listFiles()) {
            newFolder.upload(f, listener, throttle);
        }
    }

    public com.ettrema.httpclient.File upload(String name, InputStream content, Integer contentLength) throws IOException, HttpException {
        Long length = null;
        if (contentLength != null) {
            long l = contentLength;
            length = l;
        }
        return upload(name, content, length);
    }

    public com.ettrema.httpclient.File upload(String name, InputStream content, Long contentLength) throws IOException, HttpException {
        children(); // ensure children are loaded
        String newUri = href() + name;
        String contentType = URLConnection.guessContentTypeFromName(name);
        log.trace("upload: " + newUri);
        int result = host().doPut(newUri, content, contentLength, contentType);
        Utils.processResultCode(result, newUri);
        com.ettrema.httpclient.File child = new com.ettrema.httpclient.File(this, name, contentType, contentLength);
        com.ettrema.httpclient.Resource oldChild = this.child(child.name);
        if (oldChild != null) {
            this.children.remove(oldChild);
        }
        this.children.add(child);
        notifyOnChildAdded(child);
        return child;
    }

    public Folder createFolder(String name) throws IOException, HttpException {
        children(); // ensure children are loaded
        String newUri = href() + name;
        try {
            host().doMkCol(newUri);
            Folder child = new Folder(this, name);
            this.children.add(child);
            notifyOnChildAdded(child);
            return child;
        } catch (ConflictException e) {
            return handlerCreateFolderException(newUri, name);
        } catch (MethodNotAllowedException e) {
            return handlerCreateFolderException(newUri, name);
        }
    }
    
    private Folder handlerCreateFolderException(String newUri, String name) throws IOException, HttpException {
            // folder probably exists, so flush children
            System.out.println("-----------------------------------------");
            System.out.println("MKCOL method not allowed on : " + newUri);
            this.flush();
            Resource child = this.child(name);
            if (child instanceof Folder) {
                Folder fChild = (Folder) child;
                return fChild;
            } else {
                if (child == null) {
                    log.error("Couldnt create remote collection");                    
                } else {
                    log.error("Remote resource exists and is not a collection");
                }
                throw new GenericHttpException(405, newUri);
            }
        
    }

    public Resource child(String childName) throws IOException, HttpException {
//        log.trace( "child: current children: " + children().size());
        for (Resource r : children()) {
            if (r.name.equals(childName)) {
                return r;
            }
        }
        return null;
    }

    void notifyOnChildAdded(Resource child) {
        List<FolderListener> l2 = new ArrayList<FolderListener>(folderListeners);
        for (FolderListener l : l2) {
            l.onChildAdded(this, child);
        }
    }

    void notifyOnChildRemoved(Resource child) {
        List<FolderListener> l2 = new ArrayList<FolderListener>(folderListeners);
        for (FolderListener l : l2) {
            l.onChildRemoved(this, child);
        }
    }

    @Override
    public String href() {
        return super.href() + "/";
    }
}
