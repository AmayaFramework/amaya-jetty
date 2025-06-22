package io.github.amayaframework.jetty;

import io.github.amayaframework.environment.Environment;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.options.OptionSet;
import io.github.amayaframework.options.Options;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A class that implements {@link HttpServerFactory}.
 * Creates an implementations of {@link HttpServer} based on jetty {@link Server}.
 */
public class JettyServerFactory implements HttpServerFactory {

    static {
        // Preload available connector factories
        JettyProtocols.load();
    }

    private final JettyFactory factory;
    private final JettyHandlerConfigurer configurer;
    private final Path root;

    /**
     *
     * @param factory
     * @param configurer
     * @param root
     */
    public JettyServerFactory(JettyFactory factory, JettyHandlerConfigurer configurer, Path root) {
        this.factory = factory;
        this.configurer = configurer;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     *
     * @param factory
     * @param configurer
     */
    public JettyServerFactory(JettyFactory factory, JettyHandlerConfigurer configurer) {
        this(factory, configurer, null);
    }

    /**
     *
     * @param factory
     * @param root
     */
    public JettyServerFactory(JettyFactory factory, Path root) {
        this(factory, null, root);
    }

    /**
     *
     * @param configurer
     * @param root
     */
    public JettyServerFactory(JettyHandlerConfigurer configurer, Path root) {
        this.factory = null;
        this.configurer = configurer;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     *
     * @param factory
     */
    public JettyServerFactory(JettyFactory factory) {
        this(factory, null, null);
    }

    /**
     *
     * @param configurer
     */
    public JettyServerFactory(JettyHandlerConfigurer configurer) {
        this.factory = null;
        this.configurer = configurer;
        this.root = null;
    }

    /**
     *
     * @param root
     */
    public JettyServerFactory(Path root) {
        this.factory = null;
        this.configurer = null;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     *
     * @param supplier
     * @param configurer
     * @param root
     */
    public JettyServerFactory(Supplier<ThreadPool> supplier, JettyHandlerConfigurer configurer, Path root) {
        Objects.requireNonNull(supplier);
        this.factory = () -> new Server(supplier.get());
        this.configurer = configurer;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     *
     * @param supplier
     * @param configurer
     */
    public JettyServerFactory(Supplier<ThreadPool> supplier, JettyHandlerConfigurer configurer) {
        this(supplier, configurer, null);
    }

    /**
     *
     * @param supplier
     * @param root
     */
    public JettyServerFactory(Supplier<ThreadPool> supplier, Path root) {
        this(supplier, null, root);
    }

    /**
     *
     * @param supplier
     */
    public JettyServerFactory(Supplier<ThreadPool> supplier) {
        this(supplier, null, null);
    }

    /**
     *
     */
    public JettyServerFactory() {
        this.factory = null;
        this.configurer = null;
        this.root = null;
    }

    private Server createJettyServer(OptionSet options) {
        if (factory == null) {
            return new Server();
        }
        if (options == null) {
            return factory.create();
        }
        return factory.create(options);
    }

    private static void addHandler(Server server, Handler handler, OptionSet options) {
        if (options == null) {
            server.setHandler(handler);
            return;
        }
        if (options.asKey(JettyOptions.ENABLE_SESSIONS)) {
            var sessionHandler = new SessionHandler();
            sessionHandler.setHandler(handler);
            server.setHandler(sessionHandler);
        } else {
            server.setHandler(handler);
        }
    }

    private void addHandler(Server server, JettyHandler handler, OptionSet options) {
        if (configurer == null) {
            var wrap = new JettyWrapHandler(handler);
            addHandler(server, wrap, options);
            return;
        }
        if (options == null) {
            configurer.configure(server, handler);
        } else {
            configurer.configure(server, handler, options);
        }
    }

    private Path getRoot() {
        if (root == null) {
            return Path.of(".").toAbsolutePath().normalize();
        }
        return root;
    }

    private static void processBindOptions(Set<InetSocketAddress> set, OptionSet options) {
        // Add ports
        var port = options.get(JettyOptions.PORT);
        var ports = options.get(JettyOptions.PORTS);
        if (port != null) {
            set.add(new InetSocketAddress(port));
        }
        if (ports != null) {
            ports.forEach(p -> set.add(new InetSocketAddress(p)));
        }
        // Add ips
        var ip = options.get(JettyOptions.IP);
        var ips = options.get(JettyOptions.IPS);
        if (ip != null) {
            set.add(ip);
        }
        if (ips != null) {
            ips.forEach(set::add);
        }
    }

    private static void processHttpConfigOptions(JettyHttpConfig config, OptionSet options) {
        var version = options.get(JettyOptions.HTTP_VERSION);
        if (version == null) {
            return;
        }
        config.setHttpVersion(version);
    }

    private static HttpCodeBuffer getCodeBuffer(OptionSet options) {
        if (options == null) {
            return HttpCode::of;
        }
        var buffer = options.get(JettyOptions.HTTP_CODE_BUFFER);
        if (buffer == null) {
            return HttpCode::of;
        }
        return buffer;
    }

    private HttpServer createHttpServer(OptionSet set, Path root) {
        // Create jetty server
        var server = createJettyServer(set);
        // Prepare bind addresses and http config
        var addresses = new AddressSet(server, root, set == null ? Options.empty() : set);
        var config = new JettyHttpConfig(addresses);
        if (set != null) {
            processHttpConfigOptions(config, set);
            processBindOptions(addresses, set);
        }
        // Prepare jetty handler
        var buffer = getCodeBuffer(set);
        var handler = new JettyHandlerImpl(buffer);
        addHandler(server, handler, set);
        return new JettyHttpServer(server, addresses, config, handler);
    }

    private HttpServer createHttpServer(Path root) {
        // Create jetty server
        var server = createJettyServer(null);
        // Prepare bind addresses and http config
        var addresses = new AddressSet(server, root, Options.empty());
        var config = new JettyHttpConfig(addresses);
        // Prepare jetty handler
        var handler = new JettyHandlerImpl(HttpCode::of);
        addHandler(server, handler, null);
        return new JettyHttpServer(server, addresses, config, handler);
    }

    @Override
    public HttpServer create(OptionSet set, Environment env) {
        var root = env == null ? getRoot() : env.getRoot();
        return createHttpServer(set, root);
    }

    @Override
    public HttpServer create(OptionSet set) {
        var root = getRoot();
        return createHttpServer(set, root);
    }

    @Override
    public HttpServer create(Environment env) {
        var root = env == null ? getRoot() : env.getRoot();
        return createHttpServer(root);
    }

    @Override
    public HttpServer create() {
        var root = getRoot();
        return createHttpServer(root);
    }
}
