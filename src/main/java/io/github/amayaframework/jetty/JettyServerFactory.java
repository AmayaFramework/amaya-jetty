package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 *
 */
public final class JettyServerFactory implements HttpServerFactory {
    private final JettyFactory factory;
    private final Supplier<ThreadPool> supplier;

    /**
     *
     * @param factory
     */
    public JettyServerFactory(JettyFactory factory) {
        this.factory = factory;
        this.supplier = null;
    }

    /**
     *
     * @param supplier
     */
    public JettyServerFactory(Supplier<ThreadPool> supplier) {
        this.factory = null;
        this.supplier = supplier;
    }

    /**
     *
     */
    public JettyServerFactory() {
        this.factory = null;
        this.supplier = null;
    }

    private static void processBindOptions(OptionSet options, Set<InetSocketAddress> set) {
        // Add ports
        var port = options.<Integer>get(JettyOptions.PORT);
        var ports = options.<Iterable<Integer>>get(JettyOptions.PORTS);
        if (port != null) {
            set.add(new InetSocketAddress(port));
        }
        if (ports != null) {
            ports.forEach(p -> set.add(new InetSocketAddress(p)));
        }
        // Add ips
        var ip = options.<InetSocketAddress>get(JettyOptions.IP);
        var ips = options.<Iterable<InetSocketAddress>>get(JettyOptions.IPS);
        if (ip != null) {
            set.add(ip);
        }
        if (ips != null) {
            ips.forEach(set::add);
        }
    }

    private static void processHttpConfigOptions(OptionSet options, JettyHttpConfig config) {
        var version = options.<HttpVersion>get(JettyOptions.HTTP_VERSION);
        if (version == null) {
            return;
        }
        if (version.after(HttpVersion.HTTP_1_1)) {
            throw new IllegalArgumentException(version + "is not supported");
        }
        config.version = version;
    }

    private Server createServer() {
        if (supplier == null) {
            return new Server();
        }
        return new Server(supplier.get());
    }

    private Server create(Handler handler) {
        // Use factory if it is not null
        if (factory != null) {
            return Objects.requireNonNull(factory.create(handler));
        }
        // Create default server with session handler
        var ret = createServer();
        var sessionHandler = new SessionHandler();
        sessionHandler.setHandler(handler);
        ret.setHandler(sessionHandler);
        return ret;
    }

    private Server create(Handler handler, OptionSet options) {
        // Use factory if it is not null
        if (factory != null) {
            return Objects.requireNonNull(factory.create(handler, options));
        }
        // Create default server with session handler
        var ret = createServer();
        if (options.asKey(JettyOptions.ENABLE_SESSIONS)) {
            var sessionHandler = new SessionHandler();
            sessionHandler.setHandler(handler);
            ret.setHandler(sessionHandler);
        } else {
            ret.setHandler(handler);
        }
        return ret;
    }

    @Override
    public HttpServer create(OptionSet options) {
        if (options == null || options.isEmpty()) {
            return create();
        }
        var handler = new JettyHandler();
        var server = create(handler, options);
        var addresses = new AddressSet(server);
        processBindOptions(options, addresses);
        var config = new JettyHttpConfig(addresses);
        processHttpConfigOptions(options, config);
        return new JettyHttpServer(server, addresses, config, handler);
    }

    @Override
    public HttpServer create() {
        var handler = new JettyHandler();
        var server = create(handler);
        var addresses = new AddressSet(server);
        var config = new JettyHttpConfig(addresses);
        return new JettyHttpServer(server, addresses, config, handler);
    }
}
