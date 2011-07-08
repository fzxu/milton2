package com.ettrema.zsync;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.bradmcevoy.io.BufferingOutputStream;
import com.bradmcevoy.io.StreamUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * This resource factory allows resouces to be retrieved and updated using
 * the zsync protocol.
 * 
 * Process for updating a local file from a server file
 * a) assume the remote file is at path /somefile
 * b) retrieve zsync metadata (ie headers and checksums)
 *		GET /somefile/.zsync
 * c) implement rolling checksums and retrieve ranges of real file as needed with partial GETs
 *		GET /somefile
 *		Ranges: x-y, n-m, etc
 * d) merge the partial ranges
 * 
 *
 * @author brad
 */
public class ZSyncResourceFactory implements ResourceFactory {

	private String suffix = ".zsync";
	
	private ResourceFactory wrapped;
	
	private MetaFileMaker metaFileMaker;
	
	private int defaultBlockSize = 300;	
	
	private int maxMemorySize = 100000;

	public ZSyncResourceFactory(ResourceFactory wrapped) {
		this.wrapped = wrapped;
		metaFileMaker = new MetaFileMaker();
	}
		
	
	public Resource getResource(String host, String path) {
		if( path.endsWith("/" + suffix)) {
			Path p = Path.path(path);
			String realPath = p.getParent().toString();
			Resource r = wrapped.getResource(host, realPath);
			if( r == null ) {
				return null;
			} else {
				if( r instanceof GetableResource) {
					return new ZSyncAdapterResource((GetableResource)r, realPath);
				} else {
					return null;
				}
			}
		} else {
			return wrapped.getResource(host, path);
		}
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public ResourceFactory getWrapped() {
		return wrapped;
	}
	
	public class ZSyncAdapterResource implements GetableResource, ReplaceableResource, DigestResource {
		private final GetableResource r;
		private final String realPath;

		public ZSyncAdapterResource(GetableResource r, String realPath) {
			this.r = r;
			this.realPath = realPath;
		}

		public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
			Long fileLength = r.getContentLength();
			int blocksize = defaultBlockSize;
			if( fileLength != null ) {
				blocksize = metaFileMaker.computeBlockSize(fileLength);
			}			
			
			MetaFileMaker.MetaData metaData;
			if( r instanceof ZSyncResource ) {
				ZSyncResource zr = (ZSyncResource) r;
				metaData = zr.getZSyncMetaData();
			} else {
				BufferingOutputStream bufOut = new BufferingOutputStream(maxMemorySize);
                try {
                    r.sendContent( bufOut, null, params, contentType );
                    bufOut.flush();
                } catch( Exception ex ) {
                    bufOut.deleteTempFileIfExists();
                    throw new RuntimeException( ex );
                } finally {
                    StreamUtils.close( bufOut );
                }
				InputStream in = bufOut.getInputStream();
                try {
                    metaData = metaFileMaker.make(realPath, blocksize, fileLength, r.getModifiedDate(), in);
                } finally {
                    StreamUtils.close(in);
                }								
			}
			
		}
		
		public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
			throw new UnsupportedOperationException("Not supported yet.");
		}


		
		

		public Long getMaxAgeSeconds(Auth auth) {
			return null;
		}

		public String getContentType(String accepts) {
			return "application/zsyncM";
		}

		public Long getContentLength() {
			return null;
		}

		public String getUniqueId() {
			return null;
		}

		public String getName() {
			return suffix;
		}

		public Object authenticate(String user, String password) {
			return r.authenticate(user, password);
		}

		public boolean authorise(Request request, Method method, Auth auth) {
			return r.authorise(request, method, auth);
		}

		public String getRealm() {
			return r.getRealm();
		}

		public Date getModifiedDate() {
			return r.getModifiedDate();
		}

		public String checkRedirect(Request request) {
			return null;
		}

		public Object authenticate(DigestResponse digestRequest) {
			return ((DigestResource)r).authenticate(digestRequest);
		}

		public boolean isDigestAllowed() {
			return (r instanceof DigestResource) && ((DigestResource)r).isDigestAllowed();
		}
		
		
	}
}
