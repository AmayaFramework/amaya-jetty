package io.github.amayaframework.jetty;

import io.github.amayaframework.environment.Environment;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.options.OptionSet;
import io.github.amayaframework.options.Options;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
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
public class JettyServletServerFactory implements HttpServerFactory {

    static {
        // Preload available connector factories
        JettyProtocols.load();
    }

    private final JettyFactory factory;
    private final JettyContextHandlerConfigurer configurer;
    private final Path root;

    /**
     * Constructs a factory using the given Jetty server factory, handler configurer, and root directory.
     *
     * @param factory    the specified jetty server factory
     * @param configurer the specified handler configurer
     * @param root       the specified root path for the server
     */
    public JettyServletServerFactory(JettyFactory factory, JettyContextHandlerConfigurer configurer, Path root) {
        this.factory = factory;
        this.configurer = configurer;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     * Constructs a factory using the given Jetty server factory and handler configurer.
     *
     * @param factory    the specified jetty server factory
     * @param configurer the specified handler configurer
     */
    public JettyServletServerFactory(JettyFactory factory, JettyContextHandlerConfigurer configurer) {
        this(factory, configurer, null);
    }

    /**
     * Constructs a factory using the given Jetty server factory and root directory.
     *
     * @param factory the specified jetty server factory
     * @param root    the specified root path for the server
     */
    public JettyServletServerFactory(JettyFactory factory, Path root) {
        this(factory, null, root);
    }

    /**
     * Constructs a factory using the given handler configurer and root directory.
     *
     * @param configurer the specified handler configurer
     * @param root       the specified root path for the server
     */
    public JettyServletServerFactory(JettyContextHandlerConfigurer configurer, Path root) {
        this.factory = null;
        this.configurer = configurer;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     * Constructs a factory using the given Jetty server factory.
     *
     * @param factory the specified jetty server factory
     */
    public JettyServletServerFactory(JettyFactory factory) {
        this(factory, null, null);
    }

    /**
     * Constructs a factory using the given handler configurer.
     *
     * @param configurer the specified handler configurer
     */
    public JettyServletServerFactory(JettyContextHandlerConfigurer configurer) {
        this.factory = null;
        this.configurer = configurer;
        this.root = null;
    }

    /**
     * Constructs a factory using the given root directory.
     *
     * @param root the specified root path for the server
     */
    public JettyServletServerFactory(Path root) {
        this.factory = null;
        this.configurer = null;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     * Constructs a factory using the given thread pool supplier, handler configurer, and root directory.
     *
     * @param supplier   the specified thread pool supplier
     * @param configurer the specified handler configurer
     * @param root       the specified root path for the server
     */
    public JettyServletServerFactory(Supplier<ThreadPool> supplier,
                                     JettyContextHandlerConfigurer configurer,
                                     Path root) {
        Objects.requireNonNull(supplier);
        this.factory = v -> new Server(supplier.get());
        this.configurer = configurer;
        if (root != null) {
            this.root = root.toAbsolutePath().normalize();
        } else {
            this.root = null;
        }
    }

    /**
     * Constructs a factory using the given thread pool supplier and handler configurer.
     *
     * @param supplier   the specified thread pool supplier
     * @param configurer the specified handler configurer
     */
    public JettyServletServerFactory(Supplier<ThreadPool> supplier, JettyContextHandlerConfigurer configurer) {
        this(supplier, configurer, null);
    }

    /**
     * Constructs a factory using the given thread pool supplier and root directory.
     *
     * @param supplier the specified thread pool supplier
     * @param root     the specified root path for the server
     */
    public JettyServletServerFactory(Supplier<ThreadPool> supplier, Path root) {
        this(supplier, null, root);
    }

    /**
     * Constructs a factory using the given thread pool supplier.
     *
     * @param supplier the specified thread pool supplier
     */
    public JettyServletServerFactory(Supplier<ThreadPool> supplier) {
        this(supplier, null, null);
    }

    /**
     * Constructs a factory with default settings and no customization.
     */
    public JettyServletServerFactory() {
        this.factory = null;
        this.configurer = null;
        this.root = null;
    }

    private Server createJettyServer(OptionSet options, Environment env) {
        if (factory == null) {
            return new Server();
        }
        if (env == null) {
            return factory.create(options);
        }
        return factory.create(options, env);
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

    private void processSessionsOption(ServletContextHandler handler, OptionSet options) {
        if (options == null) {
            return;
        }
        if (options.asKey(JettyOptions.ENABLE_SESSIONS)) {
            handler.setSessionHandler(new SessionHandler());
        }
    }

    private ServletContextHandler createHandler(Server server, OptionSet options, Environment env) {
        var ret = new ServletContextHandler();
        processSessionsOption(ret, options);
        server.setHandler(ret);
        if (configurer == null) {
            return ret;
        }
        if (env == null) {
            configurer.configure(ret, options);
        } else {
            configurer.configure(ret, options, env);
        }
        return ret;
    }

    private HttpServer createHttpServer(OptionSet set, Path root, Environment env) {
        // Create jetty server
        var server = createJettyServer(set, env);
        // Create and init servlet handler
        var handler = createHandler(server, set, env);
        // Get servlet context
        var context = handler.getServletContext();
        // Prepare bind addresses and http config
        var addresses = new AddressSet(server, root, set == null ? Options.empty() : set);
        var config = new JettyHttpConfig(addresses, context);
        if (set != null) {
            processHttpConfigOptions(config, set);
            processBindOptions(addresses, set);
        }
        // Prepare jetty servlet
        var buffer = getCodeBuffer(set);
        var servlet = new JettyServlet(buffer);
        // Add jetty servlet to / path for generic path catch
        handler.addServlet(new ServletHolder(servlet), "/");
        return new JettyHttpServer(server, addresses, config, servlet, context);
    }

    private HttpServer createHttpServer(Path root, Environment env) {
        // Create jetty server
        var server = createJettyServer(null, env);
        // Create and init servlet handler
        var handler = new ServletContextHandler();
        server.setHandler(handler);
        if (configurer != null) {
            if (env == null) {
                configurer.configure(handler, null);
            } else {
                configurer.configure(handler, null, env);
            }
        }
        // Get servlet context
        var context = handler.getServletContext();
        // Prepare bind addresses and http config
        var addresses = new AddressSet(server, root, Options.empty());
        var config = new JettyHttpConfig(addresses, context);
        // Prepare jetty servlet
        var servlet = new JettyServlet(HttpCode::of);
        // Add jetty servlet to / path for generic path catch
        handler.addServlet(new ServletHolder(servlet), "/");
        return new JettyHttpServer(server, addresses, config, servlet, context);
    }

    @Override
    public HttpServer create(OptionSet set, Environment env) {
        var root = env == null ? getRoot() : env.getRoot();
        return createHttpServer(set, root, env);
    }

    @Override
    public HttpServer create(OptionSet set) {
        var root = getRoot();
        return createHttpServer(set, root, null);
    }

    @Override
    public HttpServer create(Environment env) {
        var root = env == null ? getRoot() : env.getRoot();
        return createHttpServer(root, env);
    }

    @Override
    public HttpServer create() {
        var root = getRoot();
        return createHttpServer(root, null);
    }
}
