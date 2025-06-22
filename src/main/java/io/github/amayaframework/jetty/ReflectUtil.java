package io.github.amayaframework.jetty;

final class ReflectUtil {
    private ReflectUtil() {
    }

    // Jetty servlet
    private static final String SERVLET_MODULE = "org.eclipse.jetty.servlet";
    private static final String SERVLET_HANDLER = "org.eclipse.jetty.servlet.ServletContextHandler";

    // Http/2.0
    private static final String ALPN_MODULE = "org.eclipse.jetty.alpn.server";
    private static final String ALPN_IMPL_MODULE = "org.eclipse.jetty.alpn.java.server";
    private static final String HTTP2_MODULE = "org.eclipse.jetty.http2.server";
    private static final String HTTP2_FACTORY = "org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory";

    // Http/3.0
    private static final String HTTP3_MODULE = "org.eclipse.jetty.http3.server";
    private static final String HTTP3_FACTORY = "org.eclipse.jetty.http3.server.HTTP3ServerConnector";

    static boolean isModuleLoaded(String name) {
        var layer = ModuleLayer.boot();
        return layer.findModule(name).isPresent();
    }

    static boolean isClassExists(String name) {
        var loader = Thread.currentThread().getContextClassLoader();
        try {
            var loaded = loader.loadClass(name);
            return loaded.getName().equals(name);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static boolean isLibraryLoaded(String module, String type) {
        return isModuleLoaded(module) || isClassExists(type);
    }
}
