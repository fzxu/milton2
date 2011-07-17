package com.ettrema.httpclient;

import com.bradmcevoy.http.Range;
import com.ettrema.httpclient.Utils.CancelledException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author mcevoyb
 */
public class File extends Resource {

    public final String contentType;
    public final Long contentLength;

    public File(Folder parent, PropFindMethod.Response resp) {
        super(parent, resp);
        this.contentType = resp.contentType;
        this.contentLength = resp.contentLength;
    }

    public File(Folder parent, String name, String contentType, Long contentLength) {
        super(parent, name);
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    public void setContent(ByteArrayInputStream in, Long contentLength) throws IOException, HttpException {
        this.parent.upload(this.name,in, contentLength);
    }

    @Override
    public String toString() {
        return super.toString() + " (content type=" + this.contentType + ")";
    }

//	/**
//	 * Downloads necessary data to produce a local copy of this server file, based
//	 * on the current local file. This uses zsync, similar to rsync, but requires
//	 * the zsync extension to the webdav server
//	 * 
//	 * The local copy is stored as a temp file, which is the returned value. It
//	 * is up to the calling application to replace the original file with the
//	 * temp file
//	 * 
//	 * @param localFile
//	 * @return - a local file holding a copy of this server file
//	 * @throws Exception
//	 * @throws HttpException 
//	 */
//	public java.io.File syncDownload(java.io.File localFile) throws Exception, HttpException {
//		return host().doSyncDownload(this, localFile); 
//	}
//	
//	public void syncUpload(java.io.File localFile) throws FileNotFoundException, HttpException, IOException {
//		host().syncUpload(this, localFile);
//	}
	

    public java.io.File downloadTo(java.io.File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, CancelledException {
        if (!destFolder.exists()) {
            throw new FileNotFoundException(destFolder.getAbsolutePath());
        }
        java.io.File dest;
        if( destFolder.isFile()) {
            // not actually a folder
            dest = destFolder;
        } else {
            dest = new java.io.File(destFolder, name);
        }
        return downloadToFile(dest, listener);
    }

    public java.io.File downloadToFile(java.io.File dest, ProgressListener listener) throws FileNotFoundException, HttpException, CancelledException {
        final FileOutputStream out;
        if (dest.exists()) {
            out = new NotifyingFileOutputStream(dest,true, listener, contentLength);
        } else {
            if (!dest.getParentFile().exists()) {
                if (!dest.getParentFile().mkdirs()) {
                    throw new FileNotFoundException("Couldnt create target directory: " + dest.getParentFile().getAbsolutePath());
                }
            }

            out = new NotifyingFileOutputStream(dest, listener, contentLength);
        }

        download(out, listener);
        return dest;
    }

    public void download(final OutputStream out, final ProgressListener listener) throws HttpException, CancelledException {
        download(out, listener, null);
    }
    
    public void download(final OutputStream out, final ProgressListener listener, List<Range> rangeList) throws HttpException, CancelledException {
        if (listener != null) {
            listener.onProgress(0, this.name);
        }
        try {
            host().doGet(href(), new StreamReceiver() {

                public void receive(InputStream in) throws IOException{
                    if( listener != null && listener.isCancelled() ) {
                        throw new RuntimeException("Download cancelled");
                    }
                    try {
                        Utils.write(in, out);
                    } catch( CancelledException cancelled) {
                        throw cancelled;
                    } catch (IOException ex) {
                        throw ex;
                    }
                }
            }, rangeList);
        } catch(CancelledException e) {
            throw e;
        } catch(Throwable e) {
        } finally {
            Utils.close(out);
        }
        if (listener != null) {
            listener.onProgress(100, this.name);
            listener.onComplete(this.name);
        }
    }
}
