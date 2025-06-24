package io.github.amayaframework.jetty;

import io.github.amayaframework.environment.Environment;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.ee9.servlet.ServletContextHandler;

/**
 * An interface describing an abstract jetty {@link ServletContextHandler} configurer.
 */
public interface JettyContextHandlerConfigurer {

    /**
     * Configures given jetty {@link ServletContextHandler} instance with given {@link OptionSet}
     * and {@link Environment}.
     *
     * @param handler     the specified {@link ServletContextHandler} instance
     * @param options     the specified {@link OptionSet} instance
     * @param environment the specified {@link Environment} instance
     */
    default void configure(ServletContextHandler handler, OptionSet options, Environment environment) {
        configure(handler, options);
    }

    /**
     * Configures given jetty {@link ServletContextHandler} instance with given {@link OptionSet}.
     *
     * @param handler the specified {@link ServletContextHandler} instance
     * @param options the specified {@link OptionSet} instance
     */
    void configure(ServletContextHandler handler, OptionSet options);
}
