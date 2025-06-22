package io.github.amayaframework.jetty.servlet;

import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * An interface describing an abstract jetty {@link ServletContextHandler} configurer.
 */
public interface JettyContextHandlerConfigurer {

    /**
     * Configures given jetty {@link ServletContextHandler} instance with given {@link OptionSet}.
     *
     * @param handler the specified {@link ServletContextHandler} instance
     * @param options the specified {@link OptionSet} instance
     */
    default void configure(ServletContextHandler handler, OptionSet options) {
        configure(handler);
    }

    /**
     * Configures given jetty {@link ServletContextHandler} instance.
     *
     * @param handler the specified {@link ServletContextHandler} instance
     */
    void configure(ServletContextHandler handler);
}
