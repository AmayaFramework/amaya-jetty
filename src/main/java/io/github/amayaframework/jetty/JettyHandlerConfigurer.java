package io.github.amayaframework.jetty;

import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Server;

/**
 * An interface describing an abstract jetty server configurer.
 */
public interface JettyHandlerConfigurer {

    /**
     * Configures given jetty {@link Server} instance with given jetty handler and {@link OptionSet}.
     * @param server the specified jetty {@link Server}  instance
     * @param handler the specified {@link JettyHandler} instance
     * @param options the specified {@link OptionSet} instance
     */
    default void configure(Server server, JettyHandler handler, OptionSet options) {
        configure(server, handler);
    }

    /**
     * Configures given jetty {@link Server} instance with given jetty handler.
     * @param server the specified jetty {@link Server}  instance
     * @param handler the specified {@link JettyHandler} instance
     */
    void configure(Server server, JettyHandler handler);
}
