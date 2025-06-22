package io.github.amayaframework.jetty.servlet;

import io.github.amayaframework.jetty.JettyFactory;
import io.github.amayaframework.jetty.JettyServerFactory;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * A class that implements {@link HttpServerFactory}.
 * Creates an implementations of {@link HttpServer} based on jetty {@link Server}.
 * Uses {@link org.eclipse.jetty.servlet.ServletContextHandler} as base jetty handler.
 */
public final class JettyServletServerFactory extends JettyServerFactory {

    /**
     * Constructs a factory using the given Jetty server factory, context servlet handler configurer and root directory.
     *
     * @param factory    the specified jetty server factory
     * @param configurer the specified  context servlet handler configurer
     * @param root       the specified root path for the server
     */
    public JettyServletServerFactory(JettyFactory factory, JettyContextHandlerConfigurer configurer, Path root) {
        super(factory, new ServletHandlerConfigurer(configurer), root);
    }

    /**
     * Constructs a factory using the given Jetty server factory and context servlet handler configurer.
     *
     * @param factory    the specified jetty server factory
     * @param configurer the specified  context servlet handler configurer
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
     * Constructs a factory using the given context servlet handler configurer and root directory.
     *
     * @param configurer the specified  context servlet handler configurer
     * @param root       the specified root path for the server
     */
    public JettyServletServerFactory(JettyContextHandlerConfigurer configurer, Path root) {
        this((JettyFactory) null, configurer, root);
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
     * Constructs a factory using the given context servlet handler configurer.
     *
     * @param configurer the specified  context servlet handler configurer
     */
    public JettyServletServerFactory(JettyContextHandlerConfigurer configurer) {
        this((JettyFactory) null, configurer, null);
    }

    /**
     * Constructs a factory using the given root directory.
     *
     * @param root the specified root path for the server
     */
    public JettyServletServerFactory(Path root) {
        this((JettyFactory) null, null, root);
    }

    /**
     * Constructs a factory using the given thread pool supplier, context servlet handler configurer and root directory.
     *
     * @param supplier   the specified thread pool supplier
     * @param configurer the specified  context servlet handler configurer
     * @param root       the specified root path for the server
     */
    public JettyServletServerFactory(Supplier<ThreadPool> supplier,
                                     JettyContextHandlerConfigurer configurer,
                                     Path root) {
        super(supplier, new ServletHandlerConfigurer(configurer), root);
    }

    /**
     * Constructs a factory using the given thread pool supplier and context servlet handler configurer.
     *
     * @param supplier   the specified thread pool supplier
     * @param configurer the specified  context servlet handler configurer
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
        this((JettyFactory) null, null, null);
    }
}
