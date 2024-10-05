package io.github.amayaframework.jetty;

/**
 * A class containing the names of the main server options supported by the {@link JettyServerFactory}.
 */
public final class JettyOptions {
    /**
     * The name of the server options group.
     *
     * @see io.github.amayaframework.options.GroupOptionSet
     */
    public static final String GROUP = "jetty";
    /**
     * The name of the listen port option. Required type: {@link Integer}.
     */
    public static final String PORT = "port";
    /**
     * The name of the listen ports option. Required type: {@link Iterable} of {@link Integer}.
     */
    public static final String PORTS = "ports";
    /**
     * The name of the listen ip option. Required type: {@link java.net.InetSocketAddress}.
     */
    public static final String IP = "ip";
    /**
     * The name of the listen ips option. Required type: {@link Iterable} of {@link java.net.InetSocketAddress}.
     */
    public static final String IPS = "ips";
    /**
     * The name of the initial http version. Required type: {@link io.github.amayaframework.http.HttpVersion}.
     */
    public static final String HTTP_VERSION = "http_version";
    /**
     * The name of the flag determines whether the server will support http sessions. Required type: {@link Boolean}.
     */
    public static final String ENABLE_SESSIONS = "enable_sessions";

    private JettyOptions() {
    }
}
