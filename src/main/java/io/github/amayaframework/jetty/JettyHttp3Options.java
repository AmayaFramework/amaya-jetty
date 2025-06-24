package io.github.amayaframework.jetty;

import com.github.romanqed.jtype.JType;
import io.github.amayaframework.options.Key;
import org.eclipse.jetty.http3.HTTP3Configuration;
import org.eclipse.jetty.quic.common.QuicConfiguration;

import java.util.function.Consumer;

/**
 * A class containing the keys for the http/3 server options supported by the {@code JettyServerFactory}.
 */
public final class JettyHttp3Options {
    private JettyHttp3Options() {
    }

    /**
     * The key for the http/3 configurer option.
     * <br>
     * Required type: {@link Consumer} of {@link HTTP3Configuration}.
     */
    public static final Key<Consumer<HTTP3Configuration>> HTTP3_CONFIGURER = Key.of(
            "http3_configurer",
            new JType<>(){}
    );

    /**
     * The key for the quic configurer option.
     * <br>
     * Required type: {@link Consumer} of {@link QuicConfiguration}
     */
    public static final Key<Consumer<QuicConfiguration>> QUIC_CONFIGURER = Key.of(
            "quic_configurer",
            new JType<>(){}
    );
}
