package io.github.amayaframework.jetty;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 * An interface describing an abstract jetty connection handler.
 */
public interface JettyHandler {

    /**
     * Handles jetty connection with given request and response descriptors.
     *
     * @param request  the specified {@link Request} instance
     * @param response the specified {@link Response} instance
     * @throws Throwable if any problems occurred
     */
    void handle(Request request, Response response) throws Throwable;
}
