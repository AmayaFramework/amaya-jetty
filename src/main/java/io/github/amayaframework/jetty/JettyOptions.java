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

    /**
     * The key for the listened port option.
     * <br>
     * Required type: {@link Integer}.
     */
    public static final Key<Integer> PORT = Key.of("port", Integer.class);

    /**
     * The key for the listened ports option.
     * <br>
     * Required type: {@link Iterable} of {@link Integer}.
     */
    public static final Key<Iterable<Integer>> PORTS = Key.of("ports", new JType<>(){});

    /**
     * The key for the listened ip address option.
     * <br>
     * Required type: {@link java.net.InetSocketAddress}.
     */
    public static final Key<InetSocketAddress> IP = Key.of("ip", InetSocketAddress.class);

    /**
     * The key for the listened ip addresses option.
     * <br>
     * Required type: {@link Iterable} of {@link java.net.InetSocketAddress}.
     */
    public static final Key<Iterable<InetSocketAddress>> IPS = Key.of("ips", new JType<>() {});

    /**
     * The key for the http version option.
     * <br>
     * Required type: {@link io.github.amayaframework.http.HttpVersion}.
     */
    public static final Key<HttpVersion> HTTP_VERSION = Key.of("http_version", HttpVersion.class);

    /**
     * The key for the flag determines whether the server will support http sessions.
     * <br>
     * Required type: {@link Boolean}.
     */
    public static final String ENABLE_SESSIONS = "enable_sessions";

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
     * The key for the ssl configs mapping option.
     * <br>
     * Required type: {@link Map} of {@link InetSocketAddress} -&gt; {@link SSLConfig}.
     */
    public static final Key<Map<InetSocketAddress, SSLConfig>> SSL_CONFIGS = Key.of("ssls", new JType<>(){});

    /**
     * The key for the http configurer option.
     * <br>
     * Required type: {@link java.util.function.BiConsumer} of {@link org.eclipse.jetty.server.HttpConfiguration}.
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

    private JettyOptions() {
    }
}
