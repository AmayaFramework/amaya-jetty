module io.github.amayaframework.jetty.servlet {
    // Imports
    // Jetty dependencies
    requires jetty.servlet.api;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    // Amaya dependencies
    requires io.github.amayaframework.options;
    requires io.github.amayaframework.context;
    requires io.github.amayaframework.server;
    requires io.github.amayaframework.jetty;
    // Exports
    exports io.github.amayaframework.jetty.servlet;
}
