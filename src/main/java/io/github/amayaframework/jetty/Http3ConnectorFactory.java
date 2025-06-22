package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.http3.HTTP3Configuration;
import org.eclipse.jetty.http3.server.HTTP3ServerConnectionFactory;
import org.eclipse.jetty.http3.server.HTTP3ServerConnector;
import org.eclipse.jetty.quic.common.QuicConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

final class Http3ConnectorFactory implements ConnectorFactory {

    private static SSLConfig getSslConfig(InetSocketAddress address, OptionSet options) {
        var found = Util.getSslConfig(address, options);
        if (found != null) {
            return found;
        }
        var common = options.get(JettyOptions.SSL_CONFIG);
        if (common == null) {
            throw new IllegalArgumentException("Cannot initialize HTTP/3 without ssl config");
        }
        return common;
    }

    private static void configure(HTTP3Configuration config, OptionSet options) {
        var configurer = options.get(JettyHttp3Options.HTTP3_CONFIGURER);
        if (configurer == null) {
            return;
        }
        configurer.accept(config);
    }

    private static void configure(QuicConfiguration config, OptionSet options) {
        var configurer = options.get(JettyHttp3Options.QUIC_CONFIGURER);
        if (configurer == null) {
            return;
        }
        configurer.accept(config);
    }

    private static void configurePemWorkDir(QuicConfiguration config,
                                            InetSocketAddress address,
                                            Path root) throws IOException {
        var dir = config.getPemWorkDirectory();
        // Check that directory existing
        if (dir != null) {
            dir = dir.toAbsolutePath().normalize();
            if (Files.isDirectory(dir)) {
                return;
            }
            Files.createDirectories(dir);
            config.setPemWorkDirectory(dir);
            return;
        }
        // Otherwise generate it
        // root/pem/host:port
        dir = root
                .resolve("pem")
                .resolve(address.getHostString() + "#" + address.getPort())
                .toAbsolutePath()
                .normalize();
        if (!Files.isDirectory(dir)) {
            Files.createDirectories(dir);
        }
        config.setPemWorkDirectory(dir);
    }

    @Override
    public Connector create(Server server, InetSocketAddress address, Path root, OptionSet options) throws IOException {
        var sslConfig = getSslConfig(address, options);
        var sslContextFactory = Util.createSslContextFactory(root, sslConfig);
        var http3Factory = new HTTP3ServerConnectionFactory();
        Util.configure(http3Factory.getHttpConfiguration(), HttpVersion.HTTP_3_0, options);
        configure(http3Factory.getHTTP3Configuration(), options);
        var ret = new HTTP3ServerConnector(server, sslContextFactory, http3Factory);
        var quicConfig = ret.getQuicConfiguration();
        configure(quicConfig, options);
        configurePemWorkDir(quicConfig, address, root);
        ret.setHost(address.getHostString());
        ret.setPort(address.getPort());
        return ret;
    }
}
