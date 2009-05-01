package com.bradmcevoy.common;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.io.File;
import java.util.Collection;

/**
 *
 * @author brad
 */
public class ContentTypeUtils {
    public static String findContentTypes( String name ) {
        Collection mimeTypes = MimeUtil.getMimeTypes( name );
        return buildContentTypeText(mimeTypes);
    }

    public static String findContentTypes( File file ) {
        Collection mimeTypes = MimeUtil.getMimeTypes( file );
        return buildContentTypeText(mimeTypes);
    }

    public static String findAcceptableContentType(String mime, String preferredList) {
        MimeType mt = MimeUtil.getPreferedMimeType(preferredList, mime);
        return mt.toString();

    }

    private static String buildContentTypeText( Collection mimeTypes ) {
        StringBuffer sb = null;
        for( Object o : mimeTypes ) {
            MimeType mt = (MimeType) o;
            if( sb == null ) {
                sb = new StringBuffer();
            } else {
                sb.append( "," );
            }
            sb.append( mt.toString() );
        }
        return sb.toString();
    }
}
