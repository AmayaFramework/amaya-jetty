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
    requires com.github.romanqed.jetty.generator;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.ee10.servlet;
    requires static org.eclipse.jetty.alpn.server;
    requires static org.eclipse.jetty.http2.server;
    requires static org.eclipse.jetty.http3.server;
    // Amaya dependencies
    requires amayaframework.options;
    requires amayaframework.server;
    requires amayaframework.service;
    requires static amayaframework.environment;
    // Exports
    exports io.github.amayaframework.jetty;
}
