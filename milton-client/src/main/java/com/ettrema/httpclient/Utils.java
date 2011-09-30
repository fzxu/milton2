package com.ettrema.httpclient;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mcevoyb
 */
public class Utils {

	public static void close(InputStream in) {
		try {
			if (in == null) {
				return;
			}
			in.close();
		} catch (IOException ex) {
			Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void close(OutputStream out) {
		try {
			if (out == null) {
				return;
			}
			out.close();
		} catch (IOException ex) {
			Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static long write(InputStream in, OutputStream out, final ProgressListener listener) throws IOException {
		long bytes = 0;
		byte[] arr = new byte[1024];
		int s = in.read(arr);
		bytes += s;
		while (s >= 0) {
			if (listener != null && listener.isCancelled()) {
				throw new CancelledException();
			}
			out.write(arr, 0, s);
			s = in.read(arr);
			bytes += s;
			listener.onProgress(bytes, null, null);
		}
		return bytes;
	}

	/**
	 * Wraps the outputstream in a bufferedoutputstream and writes to it
	 * 
	 * the outputstream is closed and flushed before returning
	 * 
	 * @param in
	 * @param out
	 * @param listener
	 * @throws IOException 
	 */
	public static long writeBuffered(InputStream in, OutputStream out, final ProgressListener listener) throws IOException {
		BufferedOutputStream bout = null;
		try {
			bout = new BufferedOutputStream(out);
			long bytes = Utils.write(in, out, listener);
			bout.flush();
			out.flush();
			return bytes;
		} finally {			
			Utils.close(bout);
			Utils.close(out);
		}

	}

	public static void processResultCode(int result, String href) throws com.ettrema.httpclient.HttpException {
		if (result >= 200 && result < 300) {
			return;
		} else if (result >= 300 && result < 400) {
			switch (result) {
				case 301:
					throw new RedirectException(result, href);
				case 302:
					throw new RedirectException(result, href);
				case 304:
					break;
				default:
					throw new RedirectException(result, href);
			}
		} else if (result >= 400 && result < 500) {
			switch (result) {
				case 400:
					throw new BadRequestException(result, href);
				case 401:
					throw new Unauthorized(result, href);
				case 403:
					throw new Unauthorized(result, href);
				case 404:
					throw new NotFoundException(result, href);
				case 405:
					throw new MethodNotAllowedException(result, href);
				case 409:
					throw new ConflictException(result, href);
				default:
					throw new GenericHttpException(result, href);
			}
		} else if (result >= 500 && result < 600) {
			throw new InternalServerError(href, result);
		} else {
			throw new GenericHttpException(result, href);
		}

	}

	public static class CancelledException extends IOException {
	}
}
