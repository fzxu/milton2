package com.ettrema.ftp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.impl.FtpHandler;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.listener.nio.AbstractListener;
import org.apache.ftpserver.listener.nio.FtpHandlerAdapter;
import org.apache.ftpserver.listener.nio.FtpLoggingFilter;
import org.apache.ftpserver.listener.nio.FtpServerProtocolCodecFactory;
import org.apache.ftpserver.listener.nio.NioListener;
import org.apache.ftpserver.ssl.ClientAuth;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.firewall.BlacklistFilter;
import org.apache.mina.filter.firewall.Subnet;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is copied from org.apache.ftpserver.impl.DefaultFtpHandler
 *
 * The only change is that this takes a FtpHandler in its constructor
 * which is what gets hooked up to Mina. This allows a custom FtpHandler
 *
 * @author brad
 */
public class MiltonListener extends AbstractListener{

    private final Logger LOG = LoggerFactory.getLogger(NioListener.class);

    private SocketAcceptor acceptor;

    private InetSocketAddress address;

    boolean suspended = false;

    private ExecutorService filterExecutor = new OrderedThreadPoolExecutor();

    private final FtpHandler handler;

    private FtpServerContext context;

    /**
     * Constructor for internal use, do not use directly. Instead use {@link ListenerFactory}
     */
    public MiltonListener(String serverAddress, int port,
            boolean implicitSsl,
            SslConfiguration sslConfiguration,
            DataConnectionConfiguration dataConnectionConfig,
            int idleTimeout, List<InetAddress> blockedAddresses, List<Subnet> blockedSubnets,FtpHandler handler) {
        super(serverAddress, port, implicitSsl, sslConfiguration, dataConnectionConfig,
                idleTimeout, blockedAddresses, blockedSubnets);
        this.handler = handler;

        updateBlacklistFilter();
    }

    private void updateBlacklistFilter() {
        if (acceptor != null) {
            BlacklistFilter filter = (BlacklistFilter) acceptor
                    .getFilterChain().get("ipFilter");

            if (filter != null) {
                if (getBlockedAddresses() != null) {
                    filter.setBlacklist(getBlockedAddresses());
                } else if (getBlockedSubnets() != null) {
                    filter.setSubnetBlacklist(getBlockedSubnets());
                } else {
                    // an empty list clears the blocked addresses
                    filter.setSubnetBlacklist(new ArrayList<Subnet>());
                }

            }
        }
    }

    /**
     * @see Listener#start(FtpServerContext)
     */
    public synchronized void start(FtpServerContext context) {
        try {

            this.context = context;

            acceptor = new NioSocketAcceptor(Runtime.getRuntime()
                    .availableProcessors());

            if (getServerAddress() != null) {
                address = new InetSocketAddress(getServerAddress(), getPort());
            } else {
                address = new InetSocketAddress(getPort());
            }

            acceptor.setReuseAddress(true);
            acceptor.getSessionConfig().setReadBufferSize(2048);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,
                    getIdleTimeout());
            // Decrease the default receiver buffer size
            ((SocketSessionConfig) acceptor.getSessionConfig())
                    .setReceiveBufferSize(512);

            MdcInjectionFilter mdcFilter = new MdcInjectionFilter();

            acceptor.getFilterChain().addLast("mdcFilter", mdcFilter);

            // add and update the blacklist filter
            acceptor.getFilterChain().addLast("ipFilter", new BlacklistFilter());
            updateBlacklistFilter();

            acceptor.getFilterChain().addLast("threadPool",
                    new ExecutorFilter(filterExecutor));
            acceptor.getFilterChain().addLast("codec",
                    new ProtocolCodecFilter(new FtpServerProtocolCodecFactory()));
            acceptor.getFilterChain().addLast("mdcFilter2", mdcFilter);
            acceptor.getFilterChain().addLast("logger", new FtpLoggingFilter());

            if (isImplicitSsl()) {
                SslConfiguration ssl = getSslConfiguration();
                SslFilter sslFilter;
                try {
                    sslFilter = new SslFilter(ssl.getSSLContext());
                } catch (GeneralSecurityException e) {
                    throw new FtpServerConfigurationException("SSL could not be initialized, check configuration");
                }

                if (ssl.getClientAuth() == ClientAuth.NEED) {
                    sslFilter.setNeedClientAuth(true);
                } else if (ssl.getClientAuth() == ClientAuth.WANT) {
                    sslFilter.setWantClientAuth(true);
                }

                if (ssl.getEnabledCipherSuites() != null) {
                    sslFilter.setEnabledCipherSuites(ssl.getEnabledCipherSuites());
                }

                acceptor.getFilterChain().addFirst("sslFilter", sslFilter);
            }

            handler.init(context, this);

            //////////////////////////////////////////

            // Here's the hack. Instead of instantiating a defaultftphandler
            // we use the one supplied in the constructor

            //////////////////////////////////////////
            acceptor.setHandler(new FtpHandlerAdapter(context, handler));

            try {
                acceptor.bind(address);
            } catch (IOException e) {
                throw new FtpServerConfigurationException("Failed to bind to address " + address + ", check configuration", e);
            }

            updatePort();

        } catch(RuntimeException e) {
            // clean up if we fail to start
            stop();

            throw e;
        }
    }

    private void updatePort() {
        // update the port to the real port bound by the listener
        setPort(acceptor.getLocalAddress().getPort());
    }

    /**
     * @see Listener#stop()
     */
    public synchronized void stop() {
        // close server socket
        if (acceptor != null) {
            acceptor.unbind();
            acceptor.dispose();
            acceptor = null;
        }

        if (filterExecutor != null) {
            filterExecutor.shutdown();
            try {
                filterExecutor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
            } finally {
                // TODO: how to handle?
            }
        }

        context = null;
    }

    /**
     * @see Listener#isStopped()
     */
    public boolean isStopped() {
        return acceptor == null;
    }

    /**
     * @see Listener#isSuspended()
     */
    public boolean isSuspended() {
        return suspended;

    }

    /**
     * @see Listener#resume()
     */
    public synchronized void resume() {
        if (acceptor != null && suspended) {
            try {
                LOG.debug("Resuming listener");
                acceptor.bind(address);
                LOG.debug("Listener resumed");

                updatePort();
            } catch (IOException e) {
                LOG.error("Failed to resume listener", e);
            }
        }
    }

    /**
     * @see Listener#suspend()
     */
    public synchronized void suspend() {
        if (acceptor != null && !suspended) {
            LOG.debug("Suspending listener");
            acceptor.unbind();

            suspended = true;
            LOG.debug("Listener suspended");
        }
    }

    /**
     * @see Listener#getActiveSessions()
     */
    public synchronized Set<FtpIoSession> getActiveSessions() {
        Map<Long, IoSession> sessions = acceptor.getManagedSessions();

        Set<FtpIoSession> ftpSessions = new HashSet<FtpIoSession>();
        for (IoSession session : sessions.values()) {
            ftpSessions.add(new FtpIoSession(session, context));
        }
        return ftpSessions;
    }
}
