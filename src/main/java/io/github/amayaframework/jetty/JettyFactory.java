package io.github.amayaframework.jetty;

import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * An interface describing an abstract jetty {@link Server} factory.
 */
public interface JettyFactory {

    /**
     * Creates a {@link Server} instance with the specified {@link Handler} implementation and {@link OptionSet}.
     *
     * @param handler the final handler that contains the logic for processing the http request, must be executed last
     * @param options the option set containing jetty server options
     * @return the {@link Server} instance
     */
    Server create(Handler handler, OptionSet options);

    /**
     * Creates a {@link Server} instance with the specified {@link Handler} implementation.
     *
     * @param handler the final handler that contains the logic for processing the http request, must be executed last
     * @return the {@link Server} instance
     */
    Server create(Handler handler);
}
