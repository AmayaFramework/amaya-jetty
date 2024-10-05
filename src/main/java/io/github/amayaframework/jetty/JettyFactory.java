package io.github.amayaframework.jetty;

import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 *
 */
public interface JettyFactory {

    /**
     * @param handler
     * @param options
     * @return
     */
    Server create(Handler handler, OptionSet options);

    /**
     * @param handler
     * @return
     */
    Server create(Handler handler);
}
