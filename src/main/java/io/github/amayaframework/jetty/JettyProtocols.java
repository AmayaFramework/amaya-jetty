package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for loading and managing support for different HTTP protocol versions
 * based on availability at runtime.
 *
 * <p>This class ensures that appropriate handlers for HTTP/2 and HTTP/3 are loaded
 * only if the corresponding support is present on the classpath.</p>
 */
public final class JettyProtocols {
    private JettyProtocols() {
    }

    private static final ConnectorFactory HTTP1_FACTORY = new Http1ConnectorFactory();
    private static final Map<HttpVersion, ConnectorFactory> FACTORIES = new ConcurrentHashMap<>();

    /**
     * Loads protocol support for HTTP/2 and HTTP/3 if the corresponding implementations are available.
     * <p>This method checks the runtime environment and conditionally registers support
     * for newer HTTP versions beyond HTTP/1.x.</p>
     */
    public static void load() {
        if (ReflectUtil.isHttp2Loaded()) {
            FACTORIES.putIfAbsent(HttpVersion.HTTP_2_0, new Http2ConnectorFactory());
        }
        if (ReflectUtil.isHttp3Loaded()) {
            FACTORIES.putIfAbsent(HttpVersion.HTTP_3_0, new Http3ConnectorFactory());
        }
    }

    static ConnectorFactory getConnectorFactory(HttpVersion version) {
        if (version.before(HttpVersion.HTTP_2_0)) {
            return HTTP1_FACTORY;
        }
        return FACTORIES.get(version);
    }
}
