/**
 * The {@code amayaframework.jetty} module provides integration of the Amaya framework with
 * the Jetty HTTP server.
 * <p>
 * This module contains implementations of the {@link io.github.amayaframework.server.HttpServer}
 * interface using Jetty as the underlying HTTP container, including support for
 * Jakarta Servlet API and modern Jetty features such as HTTP/2 and HTTP/3 (when available).
 * </p>
 *
 * <p>
 * The module depends on several utility libraries from the {@code com.github.romanqed} ecosystem
 * for functional interfaces, asynchronous programming, cancellation tokens, and type utilities.
 * It also relies on core Amaya framework modules for options, server abstractions, and service
 * lifecycle management.
 * </p>
 *
 * <p>
 * Static dependencies on Jetty ALPN, HTTP/2, and HTTP/3 modules allow optional support for these
 * protocols without forcing them at runtime.
 * </p>
 *
 * <p>
 * Typical usage involves creating and configuring a {@code JettyHttpServer} instance, binding
 * addresses and handlers, and managing lifecycle through Amaya's service framework.
 * </p>
 */
module amayaframework.jetty {
    // Imports
    // Basic dependencies
    requires com.github.romanqed.jfunc;
    requires com.github.romanqed.jsync;
    requires com.github.romanqed.juni;
    requires com.github.romanqed.jct;
    requires com.github.romanqed.jtype;
    // Jakarta servlets
    requires jakarta.servlet;
    // Jetty dependencies
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.ee10.servlet;
    requires static org.eclipse.jetty.alpn.server;
    requires static org.eclipse.jetty.http2.server;
    requires static org.eclipse.jetty.http3.server;
    requires static org.eclipse.jetty.quic.quiche.server;
    requires static org.eclipse.jetty.ee10.websocket.jakarta.server;
    // Amaya dependencies
    requires amayaframework.options;
    requires amayaframework.server;
    requires amayaframework.service;
    requires static amayaframework.environment;
    // Exports
    exports io.github.amayaframework.jetty;
}
