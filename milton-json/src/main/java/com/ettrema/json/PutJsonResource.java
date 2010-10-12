package com.ettrema.json;

import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.FileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will use milton's PUT framework to support file uploads using POST and
 * multipart encoding
 *
 * This will save the uploaded files with their given names into the parent
 * collection resource.
 *
 * If a file already exists with the same name a ConflictException is thrown,
 * unless you set the _autoname request parameter. If this parameter is present
 * (ie with any value) the file will be saved with a non-conflicting file name
 *
 * Save file information is returned as JSON in the response content
 *
 * @author brad
 */
public class PutJsonResource extends JsonResource implements PostableResource {

    private static final Logger log = LoggerFactory.getLogger( PutJsonResource.class );
    public static final String PARAM_AUTONAME = "_autoname";
    private final PutableResource wrapped;
    private final String href;
    private List<NewFile> newFiles;

    public PutJsonResource( PutableResource putableResource, String href ) {
        super( putableResource, Request.Method.PUT.code, null );
        this.wrapped = putableResource;
        this.href = href;
    }

    public String processForm( Map<String, String> parameters, Map<String, FileItem> files ) throws ConflictException {
        if( files.isEmpty() ) {
            log.debug( "no files uploaded" );
            return null;
        }
        newFiles = new ArrayList<NewFile>();
        for( FileItem file : files.values() ) {
            NewFile nf = new NewFile();
            nf.setOriginalName( file.getName() );
            nf.setContentType( file.getContentType() );
            nf.setLength( file.getSize() );
            String newName = getName( file, parameters );
            String newHref = buildNewHref( href, newName );
            nf.setHref( newHref );
            newFiles.add( nf );
            log.debug( "creating resource: " + newName + " size: " + file.getSize() );
            InputStream in = null;
            try {
                in = file.getInputStream();
                wrapped.createNew( newName, in, file.getSize(), file.getContentType() );
            } catch( NotAuthorizedException ex ) {
                throw new RuntimeException( ex );
            } catch( BadRequestException ex ) {
                throw new RuntimeException( ex );
            } catch( ConflictException ex ) {
                throw new RuntimeException( ex );
            } catch( IOException ex ) {
                throw new RuntimeException( "Exception creating resource", ex );
            } finally {
                FileUtils.close( in );
            }
        }
        return null;
    }

    /**
     * Returns a JSON representation of the newly created hrefs
     *
     * @param out
     * @param range
     * @param params
     * @param contentType
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        JsonConfig cfg = new JsonConfig();
        cfg.setIgnoreTransientFields( true );
        cfg.setCycleDetectionStrategy( CycleDetectionStrategy.LENIENT );

        NewFile[] arr = new NewFile[newFiles.size()];
        arr = newFiles.toArray( arr );
        Writer writer = new PrintWriter( out );
        JSON json = JSONSerializer.toJSON( arr, cfg );
        json.write( writer );
        writer.flush();
    }

    @Override
    public Method applicableMethod() {
        return Method.PUT;
    }

    /**
     * We dont return anything, so best not use json
     *
     * @param accepts
     * @return
     */
//    @Override
//    public String getContentType(String accepts) {
//        return "text/html";
//    }
    private String getName( FileItem file, Map<String, String> parameters ) throws ConflictException {
        String initialName = file.getName();
        boolean nonBlankName = initialName == null && initialName.trim().length() == 0;

        if( nonBlankName && wrapped.child( initialName ) == null ) {
            return file.getName();
        } else {
            String autoname = parameters.get( PARAM_AUTONAME );
            if( autoname != null ) {
                return findAcceptableName( initialName );
            } else {
                log.warn( "Conflict: Can't create resource with name " + initialName + " because it already exists. To rename automatically use request parameter: " + autoname );
                throw new ConflictException( this );
            }
        }
    }

    private String findAcceptableName( String initialName ) throws ConflictException {
        String baseName = FileUtils.stripExtension( initialName );
        String ext = FileUtils.getExtension( initialName );
        return findAcceptableName( baseName, ext, 1 );
    }

    private String findAcceptableName( String baseName, String ext, int i ) throws ConflictException {
        String candidateName = baseName + "_" + i;
        if( ext != null && ext.length() > 0 ) {
            candidateName += "." + ext;
        }
        if( wrapped.child( candidateName ) == null ) {
            return candidateName;
        } else {
            if( i < 100 ) {
                return findAcceptableName( baseName, ext, i + 1 );
            } else {
                log.warn( "Too many files with similar names: " + candidateName );
                throw new ConflictException( this );
            }
        }
    }

    private String buildNewHref( String href, String newName ) {
        String s = href;
        int pos = href.lastIndexOf( "_DAV" );
        s = s.substring( 0, pos - 1 );
        if( !s.endsWith( "/" ) ) s += "/";
        s += newName;
        return s;
    }

    public class NewFile {

        private String href;
        private String originalName;
        private long length;
        private String contentType;

        public String getHref() {
            return href;
        }

        public void setHref( String href ) {
            this.href = href;
        }

        public String getOriginalName() {
            return originalName;
        }

        public void setOriginalName( String originalName ) {
            this.originalName = originalName;
        }

        public long getLength() {
            return length;
        }

        public void setLength( long length ) {
            this.length = length;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType( String contentType ) {
            this.contentType = contentType;
        }
    }
}
