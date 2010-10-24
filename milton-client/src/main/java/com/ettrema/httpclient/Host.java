package com.ettrema.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mcevoyb
 */
public class Host extends Folder {

    private static String PROPFIND_XML = "<?xml version=\"1.0\"?>"
        + "<d:propfind xmlns:d='DAV:' xmlns:c='clyde'><d:prop>"
        + "<d:resourcetype/><d:displayname/><d:getcontentlength/><d:creationdate/><d:getlastmodified/><d:iscollection/>"
        + "<d:quota-available-bytes/><d:quota-used-bytes/><c:crc/>"
        + "</d:prop></d:propfind>";
    private static final Logger log = LoggerFactory.getLogger( Host.class );
    public final String server;
    public final int port;
    public final String user;
    public final String password;
    final HttpClient client;
    public final List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();
    private String propFindXml = PROPFIND_XML;

    static {
        //  System.setProperty("java.net.useSystemProxies", "true");
        System.setProperty( "java.net.useSystemProxies", "false" );
//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//    System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//    System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
//    System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");    
    }

    public Host( String server, int port, String user, String password ) {
        super();
        this.server = server;
        this.port = port;
        this.user = user;
        this.password = password;
        client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout( 10000 );
        if( user != null ) {
            client.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( user, password ) );
        }
        //client.getState().setProxyCredentials(AuthScope.ANY, new NTCredentials("xxx", "yyy", "", "zzz"));

        if( user != null && user.length() > 0 ) {
            client.getParams().setAuthenticationPreemptive( true );
        }
        client.getParams().setCookiePolicy( CookiePolicy.IGNORE_COOKIES );
//    HostConfiguration hostConfig = client.getHostConfiguration();
//    hostConfig.setProxy("aproxy", 80);
    }

    public Resource find( String path ) throws IOException {
        if( path == null || path.length() == 0 || path.equals( "/" ) )
            return this;
        String[] arr = path.split( "/" );
        return _find( this, arr, 0 );

    }

    public static Resource _find( Folder parent, String[] arr, int i ) throws IOException {
        String childName = arr[i];
        Resource child = parent.child( childName );
        if( i == arr.length - 1 ) {
            return child;
        } else {
            if( child instanceof Folder ) {
                return _find( (Folder) child, arr, i + 1 );
            } else {
                log.trace( "not found: " + childName );
                return null;
            }
        }
    }

    public Folder getFolder( String path ) throws IOException {
        Resource res = find( path );
        if( res instanceof Folder ) {
            return (Folder) res;
        } else {
            throw new RuntimeException( "Not a folder: " + res.href() );
        }
    }

    synchronized PropFindMethod createPropFind( int depth, String href ) {
        PropFindMethod m = new PropFindMethod( urlEncode( href ) );
        m.addRequestHeader( new Header( "Depth", depth + "" ) );
        m.setDoAuthentication( true );
        return m;
    }

    synchronized int doMkCol( String newUri ) {
        notifyStartRequest();
        MkColMethod p = new MkColMethod( urlEncode( newUri ) );
        try {
            int result = host().client.executeMethod( p );
            Utils.processResultCode( result, newUri );
            return result;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            p.releaseConnection();
            notifyFinishRequest();
        }

    }

    synchronized int doPut( String newUri, InputStream content, Long contentLength, String contentType ) {
        notifyStartRequest();
        String s = urlEncode( newUri );
        PutMethod p = new PutMethod( s );
        try {
            RequestEntity requestEntity;
            if( contentLength == null ) {
                log.trace( "no content length" );
                requestEntity = new InputStreamRequestEntity( content, contentType );
            } else {
                requestEntity = new InputStreamRequestEntity( content, contentLength, contentType );
            }
            p.setRequestEntity( requestEntity );
            int result = host().client.executeMethod( p );
            return result;
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            p.releaseConnection();
            notifyFinishRequest();
        }
    }

    synchronized int doCopy( String from, String newUri ) {
        notifyStartRequest();
        CopyMethod m = new CopyMethod( urlEncode( from ), urlEncode( newUri ) );
        try {
            int res = host().client.executeMethod( m );
            Utils.processResultCode( res, from );
            return res;
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }

    }

    synchronized int doDelete( String href ) throws IOException {
        notifyStartRequest();
        DeleteMethod m = new DeleteMethod( urlEncode( href ) );
        try {
            int res = host().client.executeMethod( m );
            Utils.processResultCode( res, href );
            return res;
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    synchronized int doMove( String href, String newUri ) throws IOException {
        notifyStartRequest();
        MoveMethod m = new MoveMethod( urlEncode( href ), urlEncode( newUri ) );
        try {
            int res = host().client.executeMethod( m );
            Utils.processResultCode( res, href );
            return res;
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }

    }

    synchronized List<PropFindMethod.Response> doPropFind( String url, int depth ) throws IOException {
        log.trace( "doPropFind: " + url );
        notifyStartRequest();
        PropFindMethod m = createPropFind( depth, url );

        try {
            if( propFindXml != null ) {
                RequestEntity requestEntity = new StringRequestEntity( propFindXml, "text/xml", "UTF-8" );
                m.setRequestEntity( requestEntity );
            }

            int res = client.executeMethod( m );
            Utils.processResultCode( res, url );
            if( res == 207 ) {
                return m.getResponses();
            } else {
                return null;
            }
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    synchronized void doGet( String url, StreamReceiver receiver ) {
        notifyStartRequest();
        GetMethod m = new GetMethod( urlEncode( url ) );
        InputStream in = null;
        try {
            int res = client.executeMethod( m );
            Utils.processResultCode( res, url );
            in = m.getResponseBodyAsStream();
            receiver.receive( in );
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            Utils.close( in );
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    public synchronized void options( String path ) throws NotFoundException, java.net.ConnectException, Unauthorized {
        String url = this.href() + path;
        log.debug( "options: " + url);
        doOptions( url );
    }

    private synchronized void doOptions( String url ) throws NotFoundException, java.net.ConnectException, Unauthorized {
        notifyStartRequest();
        GetMethod m = new GetMethod( urlEncode( url ) );
        InputStream in = null;
        try {
            int res = client.executeMethod( m );
            log.trace( "result code: " + res);
            Utils.processResultCode( res, url );
        } catch(java.net.ConnectException e) {
            throw e;
        } catch( NotFoundException e ) {
            throw e;
        } catch( Unauthorized e ) {
            throw e;
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            Utils.close( in );
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    /**
     * POSTs the variables and returns the body
     *
     * @param url
     * @param params
     * @return
     */
    String doPost( String url, Map<String, String> params ) {
        notifyStartRequest();
        PostMethod m = new PostMethod( urlEncode( url ) );
        for( Entry<String, String> entry : params.entrySet() ) {
            m.addParameter( entry.getKey(), entry.getValue() );
        }
        InputStream in = null;
        try {
            int res = client.executeMethod( m );
            Utils.processResultCode( res, url );
            in = m.getResponseBodyAsStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            IOUtils.copy( in, bout );
            return bout.toString();
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            Utils.close( in );
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    @Override
    public Host host() {
        return this;
    }

    @Override
    public String href() {
        return "http://" + server + "/";
    }

    String urlEncode( String s ) {
        s = s.replace( " ", "%20" );
        return s;
    }

    void notifyStartRequest() {
        for( ConnectionListener l : connectionListeners ) {
            l.onStartRequest();
        }
    }

    void notifyFinishRequest() {
        for( ConnectionListener l : connectionListeners ) {
            l.onFinishRequest();
        }
    }

    public String getPropFindXml() {
        return propFindXml;
    }

    public void setPropFindXml( String propFindXml ) {
        this.propFindXml = propFindXml;
    }
}
