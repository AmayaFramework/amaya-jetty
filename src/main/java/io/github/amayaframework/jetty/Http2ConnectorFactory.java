package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;

import java.net.InetSocketAddress;
import java.nio.file.Path;

final class Http2ConnectorFactory implements ConnectorFactory {

    private static void configure(HTTP2ServerConnectionFactory factory, OptionSet options) {
        var configurer = options.get(JettyHttp2Options.HTTP2_CONFIGURER);
        if (configurer == null) {
            return;
        }
        configurer.accept(factory);
    }

    private static ConnectionFactory[] createH2C(HttpConfiguration config, OptionSet options) {
        var http1Factory = new HttpConnectionFactory(config);
        var http2Factory = new HTTP2CServerConnectionFactory(config);
        configure(http2Factory, options);
        // h1, h2
        return new ConnectionFactory[]{http1Factory, http2Factory};
    }

    private static ConnectionFactory[] createH2(Path root,
                                                HttpConfiguration httpConfig,
                                                SSLConfig sslConfig,
                                                OptionSet options) {
        var http1Factory = new HttpConnectionFactory(httpConfig);
        var http2Factory = new HTTP2ServerConnectionFactory(httpConfig);
        configure(http2Factory, options);
        var alpnFactory = new ALPNServerConnectionFactory();
        alpnFactory.setDefaultProtocol(http1Factory.getProtocol());
        var contextFactory = Util.createSslContextFactory(root, sslConfig);
        var sslFactory = new SslConnectionFactory(contextFactory, alpnFactory.getProtocol());
        // ssl, alpn, h2, h1
        return new ConnectionFactory[]{sslFactory, alpnFactory, http2Factory, http1Factory};
    }

    @Override
    public Connector create(Server server, InetSocketAddress address, Path root, OptionSet options) {
        var config = new HttpConfiguration();
        Util.configure(config, HttpVersion.HTTP_2_0, options);
        var sslConfig = Util.getSslConfig(address, options);
        var factories = (ConnectionFactory[]) null;
        if (sslConfig == null) {
            factories = createH2C(config, options);
        } else {
            Util.addSecure(config, address.getPort(), options);
            try {
                factories = createH2(root, config, sslConfig, options);
            } catch (NoClassDefFoundError e) {
                throw new IllegalStateException("ALPN module not loaded", e);
            }
        }
        return Util.createConnector(server, address, factories, options);
    }
}
