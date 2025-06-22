package io.github.amayaframework.jetty;

import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Server;

/**
 * An interface describing an abstract jetty {@link Server} factory.
 */
public interface JettyFactory {

    /**
     * Creates a {@link Server} instance with the specified {@link OptionSet}.
     *
     * @param options the option set containing jetty server options
     * @return the {@link Server} instance
     */
    default Server create(OptionSet options) {
        return create();
    }

    /**
     * Creates a {@link Server} instance.
     *
     * @return the {@link Server} instance
     */
    Server create();
}
