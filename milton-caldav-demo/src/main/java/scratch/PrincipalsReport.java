package scratch;

import com.ettrema.httpclient.ReportMethod;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 *
 * @author brad
 */
public class PrincipalsReport {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        HttpClient client = new HttpClient();
        ReportMethod rm = new ReportMethod( "http://localhost:9080/principals");
        rm.setRequestHeader( "depth", "0");
        rm.setRequestEntity( new StringRequestEntity( "<x0:principal-search-property-set xmlns:x0=\"DAV:\"/>", "text/xml", "UTF-8"));
        int result = client.executeMethod( rm );
        System.out.println( "result: " + result );
        String resp = rm.getResponseBodyAsString();
        System.out.println( "response--" );
        System.out.println( resp );
    }

}
