package com.ettrema.http.caldav.demo;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class TContact extends TResource implements GetableResource, ReplaceableResource {

    private static final Logger log = LoggerFactory.getLogger( TContact.class );
    private String data;

    public TContact( TFolderResource parent, String name ) {
        super( parent, name );
    }

    @Override
    protected Object clone( TFolderResource newParent ) {
        TContact e = new TContact( (TCalendarResource) newParent, name );
        e.setData( data );
        return e;
    }

	@Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException, BadRequestException {
        out.write( data.getBytes() );
    }

	@Override
    public String getContentType( String accepts ) {
        return "text/vcard";
    }

	@Override
	public void replaceContent(InputStream in, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			StreamUtils.readTo(in, bout);
		} catch (ReadingException ex) {
			throw new RuntimeException(ex);
		} catch (WritingException ex) {
			throw new RuntimeException(ex);
		}
		this.data = bout.toString(); // should check character encoding
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	
}
