package com.ettrema.httpclient;

import com.ettrema.http.DataRange;
import java.util.List;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements the HTTP GET method.
 * <p>
 * The HTTP GET method is defined in section 9.3 of
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC2616</a>:
 * <blockquote>
 * The GET method means retrieve whatever information (in the form of an
 * entity) is identified by the Request-URI. If the Request-URI refers
 * to a data-producing process, it is the produced data which shall be
 * returned as the entity in the response and not the source text of the
 * process, unless that text happens to be the output of the process.
 * </blockquote>
 * </p>
 * <p>
 * GetMethods will follow redirect requests from the http server by default.
 * This behavour can be disabled by calling setFollowRedirects(false).</p>
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Sung-Gu Park
 * @author Sean C. Sullivan
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * 
 * @version $Revision: 480424 $
 * @since 1.0
 */
public class RangedGetMethod extends HttpMethodBase {

    // -------------------------------------------------------------- Constants
    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(RangedGetMethod.class);

    public RangedGetMethod(String uri, List<DataRange> dataRanges) {
        super(uri);    
        if (dataRanges != null) {
            String rangeHeaderVal = getRangesRequest(dataRanges);
            setRequestHeader("Range", "bytes=" + rangeHeaderVal);
        }
    }

    private String getRangesRequest(List<DataRange> ranges) {
        StringBuilder sb = new StringBuilder();
        for (DataRange d : ranges) {
            sb.append(d.getRange()).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public String getName() {
        return "GET";
    }

    // ------------------------------------------------------------- Properties
    /**
     * Recycles the HTTP method so that it can be used again.
     * Note that all of the instance variables will be reset
     * once this method has been called. This method will also
     * release the connection being used by this HTTP method.
     * 
     * @see #releaseConnection()
     * 
     * @since 1.0
     * 
     * @deprecated no longer supported and will be removed in the future
     *             version of HttpClient
     */
    public void recycle() {
        LOG.trace("enter GetMethod.recycle()");

        super.recycle();
        setFollowRedirects(true);
    }
}
