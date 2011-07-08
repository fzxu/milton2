package com.ettrema.zsync;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This resource factory allows resouces to be retrieved and updated using
 * the zsync protocol.
 * 
 * Client side process for updating a local file from a server file
 * a) assume the remote file is at path /somefile
 * b) retrieve zsync metadata (ie headers and checksums)
 *		GET /somefile/.zsync
 * c) implement rolling checksums and retrieve ranges of real file as needed with partial GETs
 *		GET /somefile
 *		Ranges: x-y, n-m, etc
 * d) merge the partial ranges
 * 
 * 
 * Client side process for updating a server file with a local file
 * a) assume the remote file is at path /somefile
 * b) Find the data ranges to update by POSTing local metadata (headers+checksums)
 *		PUT /somefile/.zsync
 *		Version: zsync-1.0.0
 *		Blocksize: 256
 * 
 *      (eg response)
 *		1222-1756
 *		20000-20512
 * c) Upload the metadata again and the checksums in a PUT
 *		
 *
 * @author brad
 */
public class ZSyncResourceFactory implements ResourceFactory {

	private static final Logger log = LoggerFactory.getLogger(ZSyncResourceFactory.class);
	
	private String suffix = ".zsync";
	
	private ResourceFactory wrapped;
	
	private MetaFileMaker metaFileMaker;
	
	private FileMaker fileMaker;
	
	private int defaultBlockSize = 512;	
	
	private int maxMemorySize = 100000;

	public ZSyncResourceFactory(ResourceFactory wrapped) {
		this.wrapped = wrapped;
		metaFileMaker = new MetaFileMaker();
		fileMaker = new FileMaker();
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
	
	public class ZSyncAdapterResource implements PostableResource, GetableResource, ReplaceableResource, DigestResource {
		private final GetableResource r;
		private final String realPath;
		
		/**
		 * populated on POST, then used in sendContent
		 */
		private List<Range> ranges;

		public ZSyncAdapterResource(GetableResource r, String realPath) {
			this.r = r;
			this.realPath = realPath;
		}
		
		public String processForm(Map<String, String> parameters, Map<String, FileItem> files) throws BadRequestException, NotAuthorizedException, ConflictException {
			System.out.println("processForm: parameters: " + parameters + " files: " + files);
			
			if( files.isEmpty()) {
				log.warn("No meta file provided");
				throw new BadRequestException(r);
			} else {
				try {
					FileItem item = files.values().iterator().next();
					// todo: this needs some refactoring
					File metaFile = File.createTempFile("milton_zsync", null);
					FileOutputStream fout = new FileOutputStream(metaFile);
					StreamUtils.readTo(item.getInputStream(), fout);
					fout.close();
					
					// copy content to a file
					File tempData = File.createTempFile("milton-zsync", null);
					FileOutputStream fDataOut = new FileOutputStream(tempData);
					r.sendContent(fDataOut, null, null, null);
					fDataOut.close();
					
					// build the list of required ranges
					ranges = fileMaker.findMissingRanges(tempData, metaFile);
					
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			return null;
		}
		
		public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {			
			if( ranges != null ) {
				log.info("sendContent: sending range data");
				sendRangeData(out);
			} else {
				log.info("sendContent: sending meta data");
				sendMetaData(params, contentType, out);
			}
			
		}

		private void sendMetaData(Map<String, String> params, String contentType, OutputStream out) throws RuntimeException {
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
			metaFileMaker.write(metaData, out);
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

		private void sendRangeData(OutputStream out) {
			PrintWriter pw = new PrintWriter(out);		
			for(Range range : ranges ) {
				pw.println(range.getRange());
			}
			pw.flush();
		}
	}
}
