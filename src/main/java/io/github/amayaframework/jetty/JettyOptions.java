package io.github.amayaframework.jetty;

import com.github.romanqed.jtype.JType;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.jetty.old.JettyServerFactory;
import io.github.amayaframework.options.Key;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * A class containing the keys for the server options supported by the {@link JettyServerFactory}.
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
    public static final Key<Iterable<InetSocketAddress>> IPS = Key.of("ips", new JType<>(){});
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
     * The key for the common ssl keystore option.
     * <br>
     * Required type: {@link SSLKeystore}.
     */
    public static final Key<SSLKeystore> KEYSTORE = Key.of("keystore", SSLKeystore.class);

    /**
     * The key for the ssl keystore mapping option.
     * <br>
     * Required type: {@link Map} of {@link InetSocketAddress} -&gt; {@link SSLKeystore}.
     */
    public static final Key<Map<InetSocketAddress, SSLKeystore>> KEYSTORES = Key.of("keystores", new JType<>(){});

    private JettyOptions() {
    }
}
