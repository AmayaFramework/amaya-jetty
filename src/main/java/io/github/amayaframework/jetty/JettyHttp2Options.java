package io.github.amayaframework.jetty;

import com.github.romanqed.jtype.JType;
import io.github.amayaframework.options.Key;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;

import java.util.function.Consumer;

/**
 * A class containing the keys for the http/2 server options supported by the {@code JettyServerFactory}.
 */
public final class JettyHttp2Options {
    private JettyHttp2Options() {
    }

    /**
     * The key for the http/2 factory configurer option.
     * <br>
     * Required type: {@link Consumer} of {@link HTTP2ServerConnectionFactory}.
     */
    public static final Key<Consumer<HTTP2ServerConnectionFactory>> HTTP2_CONFIGURER = Key.of(
            "http2_configurer",
            new JType<>(){}
    );
}
