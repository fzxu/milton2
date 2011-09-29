package com.ettrema.httpclient;

import com.bradmcevoy.http.Range;
import com.ettrema.httpclient.Utils.CancelledException;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
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
		this.parent.upload(this.name, in, contentLength, null);
	}

	@Override
	public String toString() {
		return super.toString() + " (content type=" + this.contentType + ")";
	}

	@Override
	public java.io.File downloadTo(java.io.File destFolder, ProgressListener listener) throws FileNotFoundException, IOException, HttpException, CancelledException {
		if (!destFolder.exists()) {
			throw new FileNotFoundException(destFolder.getAbsolutePath());
		}
		java.io.File dest;
		if (destFolder.isFile()) {
			// not actually a folder
			dest = destFolder;
		} else {
			dest = new java.io.File(destFolder, name);
		}
		downloadToFile(dest, listener);
		return dest;
	}

	public void downloadToFile(java.io.File dest, ProgressListener listener) throws FileNotFoundException, HttpException, CancelledException {
		if (listener != null) {
			listener.onProgress(0, this.name);
		}
		try {
			host().doGet(href(), dest, listener);
		} catch (CancelledException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		if (listener != null) {
			listener.onProgress(100, this.name);
			listener.onComplete(this.name);
		}
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

				@Override
				public void receive(InputStream in) throws IOException {
					if (listener != null && listener.isCancelled()) {
						throw new RuntimeException("Download cancelled");
					}
					try {
						Utils.write(in, out, listener);
					} catch (CancelledException cancelled) {
						throw cancelled;
					} catch (IOException ex) {
						throw ex;
					}
				}
			}, rangeList, listener);
		} catch (CancelledException e) {
			throw e;
		} catch (Throwable e) {
		} finally {
			Utils.close(out);
		}
		if (listener != null) {
			listener.onProgress(100, this.name);
			listener.onComplete(this.name);
		}
	}
}
