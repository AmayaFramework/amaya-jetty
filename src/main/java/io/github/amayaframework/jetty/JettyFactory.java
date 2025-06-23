package io.github.amayaframework.jetty;

import io.github.amayaframework.environment.Environment;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Server;

/**
 * An interface describing an abstract jetty {@link Server} factory.
 */
public interface JettyFactory {

    /**
     * Creates a {@link Server} instance with the specified {@link OptionSet} and {@link Environment}.
     * @param options the option set containing jetty server options
     * @param environment the specified {@link Environment} instance
     * @return the {@link Server} instance
     */
    default Server create(OptionSet options, Environment environment) {
        return create(options);
    }

    /**
     * Creates a {@link Server} instance with the specified {@link OptionSet}.
     *
     * @param options the option set containing jetty server options
     * @return the {@link Server} instance
     */
    Server create(OptionSet options);
}
