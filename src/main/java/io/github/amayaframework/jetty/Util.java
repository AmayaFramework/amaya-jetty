package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import io.github.amayaframework.server.ServerOptions;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.net.InetSocketAddress;
import java.nio.file.Path;

final class Util {
    private Util() {
    }

    static Path resolve(Path root, Path path) {
        if (path == null) {
            return null;
        }
        if (!path.isAbsolute()) {
            return root.resolve(path).normalize();
        }
        return path;
    }

    static SslContextFactory.Server createSslContextFactory(Path root, SSLConfig config) {
        var ret = new SslContextFactory.Server();
        var path = resolve(root, config.getPath());
        if (path != null) {
            ret.setKeyStorePath(path.toString());
        }
        config.getConfigurer().accept(ret);
        return ret;
    }

    static ServerConnector createConnector(Server server,
                                           InetSocketAddress address,
                                           ConnectionFactory[] factories,
                                           OptionSet options) {
        var acceptors = options.get(JettyOptions.ACCEPTORS, -1);
        var selectors = options.get(JettyOptions.SELECTORS, -1);
        if (acceptors == null) {
            throw new IllegalArgumentException("Acceptors option must be not null");
        }
        if (selectors == null) {
            throw new IllegalArgumentException("Selectors option must be not null");
        }
        var ret = new ServerConnector(server, acceptors, selectors, factories);
        ret.setHost(address.getHostString());
        ret.setPort(address.getPort());
        return ret;
    }

    static void addSecure(HttpConfiguration config, int port, OptionSet options) {
        config.setSecureScheme("https");
        config.setSecurePort(port);
        var customizer = new SecureRequestCustomizer();
        Util.configure(customizer, options);
        config.addCustomizer(customizer);
    }

    static void configure(HttpConfiguration config, HttpVersion version, OptionSet options) {
        config.setSendServerVersion(options.asKey(ServerOptions.SEND_SERVER));
        config.setSendXPoweredBy(options.asKey(ServerOptions.SEND_POWERED_BY));
        var configurer = options.get(JettyOptions.HTTP_CONFIGURER);
        if (configurer == null) {
            return;
        }
        configurer.accept(version, config);
    }

    static void configure(SecureRequestCustomizer customizer, OptionSet options) {
        var configurer = options.get(JettyOptions.SECURE_CONFIGURER);
        if (configurer == null) {
            return;
        }
        configurer.accept(customizer);
    }
}
