/**
 * Amaya server implementation based on jetty server.
 * @author Roman Bakaldin
 */
module io.github.amayaframework.jetty {
    // Imports
    // Basic dependencies
    requires com.github.romanqed.jfunc;
    requires com.github.romanqed.jtype;
    // Jetty dependencies
    requires jetty.servlet.api;
    requires com.github.romanqed.jetty.generator;
    requires org.eclipse.jetty.server;
    requires static org.eclipse.jetty.alpn.server;
    requires static org.eclipse.jetty.http2.server;
    requires static org.eclipse.jetty.http3.server;
    // Amaya dependencies
    requires io.github.amayaframework.http;
    requires io.github.amayaframework.options;
    requires io.github.amayaframework.context;
    requires io.github.amayaframework.service;
    requires io.github.amayaframework.server;
    requires static io.github.amayaframework.environment;
    // Exports
    exports io.github.amayaframework.jetty;
}
