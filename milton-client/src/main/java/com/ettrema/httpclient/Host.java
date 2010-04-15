package com.ettrema.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

/**
 *
 * @author mcevoyb
 */
public class Host extends Folder {

    public final String server;
    public final int port;
    public final String user;
    public final String password;
    final HttpClient client;
    public final List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();

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
        client.getState().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( user, password ) );
        //client.getState().setProxyCredentials(AuthScope.ANY, new NTCredentials("xxx", "yyy", "", "zzz"));

        client.getParams().setAuthenticationPreemptive( true );
//    HostConfiguration hostConfig = client.getHostConfiguration();
//    hostConfig.setProxy("aproxy", 80);
    }

    public Resource find( String path ) {
        if( path == null || path.length() == 0 || path.equals( "/" ) )
            return this;
        String[] arr = path.split( "/" );
        return _find( this, arr, 0 );

    }

    public static Resource _find( Folder parent, String[] arr, int i ) {
        String childName = arr[i];
        Resource child = parent.child( childName );
        if( i == arr.length - 1 ) {
            return child;
        } else {
            if( child instanceof Folder ) {
                return _find( (Folder) child, arr, i + 1 );
            } else {
                return null;
            }
        }
    }

    public Folder getFolder( String path ) {
        Resource res = find( path );
        if( res instanceof Folder ) {
            return (Folder) res;
        } else {
            throw new RuntimeException( "Not a folder: " + res.href() );
        }
    }

    PropFindMethod createPropFind( int depth, String href ) {
        PropFindMethod m = new PropFindMethod( urlEncode( href ) );
        m.addRequestHeader( new Header( "Depth", depth + "" ) );
        m.setDoAuthentication( true );
        return m;
    }

    int doMkCol( String newUri ) {
        notifyStartRequest();
        MkColMethod p = new MkColMethod( urlEncode( newUri ) );
        try {
            int result = host().client.executeMethod( p );
            Utils.processResultCode( result, href() );
            return result;
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            p.releaseConnection();
            notifyFinishRequest();
        }

    }

    int doPut( String newUri, InputStream content, Long contentLength, String contentType ) {
        notifyStartRequest();
        String s = urlEncode( newUri );
        PutMethod p = new PutMethod( s );
        try {
            RequestEntity requestEntity;
            if( contentLength == null ) {
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

    int doCopy( String from, String newUri ) {
        notifyStartRequest();
        CopyMethod m = new CopyMethod( urlEncode( from ), urlEncode( newUri ) );
        try {
            int res = host().client.executeMethod( m );
            System.out.println( "result: " + res );
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

    int doDelete( String href ) {
        notifyStartRequest();
        DeleteMethod m = new DeleteMethod( urlEncode( href ) );
        try {
            int res = host().client.executeMethod( m );
            Utils.processResultCode( res, href );
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

    int doMove( String href, String newUri ) {
        notifyStartRequest();
        MoveMethod m = new MoveMethod( urlEncode( href ), urlEncode( newUri ) );
        try {
            int res = host().client.executeMethod( m );
            System.out.println( "result: " + res );
            Utils.processResultCode( res, href );
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

    List<PropFindMethod.Response> doPropFind( String url, int depth ) {
        notifyStartRequest();
        PropFindMethod m = createPropFind( depth, url );
        try {
            int res = client.executeMethod( m );
            Utils.processResultCode( res, url );
            if( res == 207 ) {
                return m.getResponses();
            } else {
                return null;
            }
        } catch( HttpException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        } finally {
            m.releaseConnection();
            notifyFinishRequest();
        }
    }

    void doGet( String url, StreamReceiver receiver ) {
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
}
