package io.github.amayaframework.jetty;

import com.github.romanqed.jtype.JType;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.Key;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A class containing the keys for the common server options supported by the {@code JettyServerFactory}.
 */
public final class JettyOptions {
    private JettyOptions() {
    }

    /**
     * The key for the flag determines whether the server will support http sessions.
     * <br>
     * Required type: {@link Boolean}.
     */
    public static final String ENABLE_SESSIONS = "enable_sessions";

    /**
     * The key for the flag determines whether the server will support websocket protocol.
     * <br>
     * Required type: {@link Boolean}.
     */
    public static final String ENABLE_WEBSOCKET = "enable_ws";

    /**
     * The key for the flag determines whether the server will prefer async mode.
     * <br>
     * Required type: {@link Boolean}.
     */
    public static final Key<Boolean> PREFER_ASYNC = Key.of("prefer_async", Boolean.class);

    /**
     * The key for the http method buffer option.
     * <br>
     * Required type: {@link HttpMethodBuffer}
     */
    public static final Key<HttpMethodBuffer> HTTP_METHOD_BUFFER = Key.of(
            "http_method_buffer",
            HttpMethodBuffer.class
    );

    /**
     * The key for the http code buffer option.
     * <br>
     * Required type: {@link HttpCodeBuffer}.
     */
    public static final Key<HttpCodeBuffer> HTTP_CODE_BUFFER = Key.of("http_code_buffer", HttpCodeBuffer.class);

    /**
     * The key for the common ssl config option.
     * <br>
     * Required type: {@link SSLConfig}.
     */
    public static final Key<SSLConfig> SSL_CONFIG = Key.of("ssl", SSLConfig.class);

    /**
     * The prefix for the key for the address bound ssl config.
     * <br>
     * Required type: {@link Map} of {@link InetSocketAddress} -&gt; {@link SSLConfig}.
     */
    public static final String SSL_CONFIG_PREFIX = "ssl.";

    /**
     * The key for the http configurer option.
     * <br>
     * Required type: {@link BiConsumer} of {@link HttpConfiguration}.
     */
    public static final Key<BiConsumer<HttpVersion, HttpConfiguration>> HTTP_CONFIGURER = Key.of(
            "http_configurer",
            new JType<>(){}
    );

    /**
     * The key for the acceptors option.
     * <br>
     * Required type: {@link Integer}.
     */
    public static final Key<Integer> ACCEPTORS = Key.of("acceptors", Integer.class);

    /**
     * The key for the selectors option.
     * <br>
     * Required type: {@link Integer}.
     */
    public static final Key<Integer> SELECTORS = Key.of("selectors", Integer.class);

    /**
     * The key for the secure configurer option.
     * <br>
     * Required type: {@link Consumer} of {@link SecureRequestCustomizer}.
     */
    public static final Key<Consumer<SecureRequestCustomizer>> SECURE_CONFIGURER = Key.of(
            "secure_configurer",
            new JType<>(){}
    );

    /**
     * Creates an option key string for an {@link SSLConfig} bound to the specified network address.
     *
     * @param address the {@link InetSocketAddress} to bind the SSL configuration to
     * @return a string key in the format {@code ssl.<host>:<port>}
     */
    public static String sslStringKey(InetSocketAddress address) {
        return SSL_CONFIG_PREFIX + address.getHostString() + ":" + address.getPort();
    }

    /**
     * Creates an option key string for an {@link SSLConfig} bound to the specified host and port.
     *
     * @param host the hostname to bind the SSL configuration to
     * @param port the port number to bind the SSL configuration to
     * @return a string key in the format {@code ssl.<host>:<port>}
     */
    public static String sslStringKey(String host, int port) {
        return SSL_CONFIG_PREFIX + host + ":" + port;
    }

    /**
     * Creates an option key string for an {@link SSLConfig} bound to all network interfaces
     * on the specified port.
     *
     * @param port the port number to bind the SSL configuration to
     * @return a string key in the format {@code ssl.0.0.0.0:<port>}
     */
    public static String sslStringKey(int port) {
        return SSL_CONFIG_PREFIX + "0.0.0.0:" + port;
    }

    /**
     * Creates a {@link Key} instance for an {@link SSLConfig} bound to the specified network address.
     *
     * @param address the {@link InetSocketAddress} to bind the SSL configuration to
     * @return a {@link Key} referencing an {@link SSLConfig} for the given address
     */
    public static Key<SSLConfig> sslKey(InetSocketAddress address) {
        return Key.of(sslStringKey(address), SSLConfig.class);
    }

    /**
     * Creates a {@link Key} instance for an {@link SSLConfig} bound to the specified host and port.
     *
     * @param host the hostname to bind the SSL configuration to
     * @param port the port number to bind the SSL configuration to
     * @return a {@link Key} referencing an {@link SSLConfig} for the given host and port
     */
    public static Key<SSLConfig> sslKey(String host, int port) {
        return Key.of(sslStringKey(host, port), SSLConfig.class);
    }

    /**
     * Creates a {@link Key} instance for an {@link SSLConfig} bound to all network interfaces
     * on the specified port.
     *
     * @param port the port number to bind the SSL configuration to
     * @return a {@link Key} referencing an {@link SSLConfig} for all interfaces on the given port
     */
    public static Key<SSLConfig> sslKey(int port) {
        return Key.of(sslStringKey(port), SSLConfig.class);
    }
}
