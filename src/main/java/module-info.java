/**
 * Amaya server implementation based on jetty server.
 * @author Roman Bakaldin
 */
module io.github.amayaframework.jetty {
    // Imports
    requires com.github.romanqed.jfunc;
    requires jetty.servlet.api;
    requires org.eclipse.jetty.server;
    requires io.github.amayaframework.options;
    requires io.github.amayaframework.http;
    requires io.github.amayaframework.context;
    requires io.github.amayaframework.server;
    // Exports
    exports io.github.amayaframework.jetty;
}
