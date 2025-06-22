package io.github.amayaframework.jetty;

import com.github.romanqed.jetty.generator.StrictHttpConnectionFactory;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.*;

import java.net.InetSocketAddress;
import java.nio.file.Path;

final class Http1ConnectorFactory implements ConnectorFactory {

    private static ConnectionFactory[] createFactories(Path root,
                                                       HttpConfiguration config,
                                                       SSLConfig sslConfig,
                                                       OptionSet options) {
        var httpFactory = new StrictHttpConnectionFactory(config);
        Util.configure(httpFactory, options);
        if (sslConfig == null) {
            return new ConnectionFactory[]{httpFactory};
        }
        var contextFactory = Util.createSslContextFactory(root, sslConfig);
        var sslFactory = new SslConnectionFactory(contextFactory, httpFactory.getProtocol());
        return new ConnectionFactory[]{sslFactory, httpFactory};
    }

    @Override
    public Connector create(Server server, InetSocketAddress address, Path root, OptionSet options) {
        var config = new HttpConfiguration();
        Util.configure(config, HttpVersion.HTTP_1_1, options);
        var sslConfig = Util.getSslConfig(address, options);
        if (sslConfig != null) {
            Util.addSecure(config, address.getPort(), options);
        }
        var factories = createFactories(root, config, sslConfig, options);
        return Util.createConnector(server, address, factories, options);
    }
}
