package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable0;
import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.*;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;

final class JettyHttpConfig implements HttpServerConfig {
    private static final MimeFormatter DEFAULT_FORMATTER = new StandardMimeFormatter();
    private static final MimeParser DEFAULT_PARSER = new StandardMimeParser();
    private static final PathTokenizer DEFAULT_TOKENIZER = new SplitPathTokenizer();

    final AddressSet addresses;
    final ServletContext context;
    Runnable1<ServletConfig> onInit;
    Runnable0 onDestroy;
    HttpVersion version;
    PathTokenizer tokenizer;
    MimeParser parser;
    MimeFormatter formatter;

    JettyHttpConfig(AddressSet addresses, ServletContext context) {
        this.addresses = addresses;
        this.context = context;
        this.version = HttpVersion.HTTP_1_1;
        this.addresses.version = HttpVersion.HTTP_1_1;
        this.tokenizer = DEFAULT_TOKENIZER;
        this.parser = DEFAULT_PARSER;
        this.formatter = DEFAULT_FORMATTER;
    }

    @Override
    public ServletContext servletContext() {
        return context;
    }

    @Override
    public Runnable1<ServletConfig> onServletInit() {
        return onInit;
    }

    @Override
    public void onServletInit(Runnable1<ServletConfig> action) {
        this.onInit = action;
    }

    @Override
    public Runnable0 onServletDestroy() {
        return onDestroy;
    }

    @Override
    public void onServletDestroy(Runnable0 action) {
        this.onDestroy = action;
    }

    @Override
    public HttpVersion httpVersion() {
        return version;
    }

    @Override
    public void httpVersion(HttpVersion version) {
        Objects.requireNonNull(version);
        if (version.before(HttpVersion.HTTP_1_1)) {
            throw new IllegalArgumentException("Minimal allowed version is HTTP/1.1");
        }
        if (version.after(HttpVersion.HTTP_3_0)) {
            throw new IllegalArgumentException("Maximum supported http version is HTTP/3.0");
        }
        if (!JettyProtocols.isVersionSupported(version)) {
            throw new IllegalArgumentException(version + " is supported, but the required dependencies is not loaded");
        }
        this.version = version;
        this.addresses.version = version;
    }

    @Override
    public void addAddress(InetSocketAddress address, HttpVersion version) {
        this.addresses.add(address, version);
    }

    @Override
    public MimeFormatter mimeFormatter() {
        return formatter;
    }

    @Override
    public void mimeFormatter(MimeFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
    }

    @Override
    public MimeParser mimeParser() {
        return parser;
    }

    @Override
    public void mimeParser(MimeParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public PathTokenizer pathTokenizer() {
        return tokenizer;
    }

    @Override
    public void pathTokenizer(PathTokenizer tokenizer) {
        this.tokenizer = Objects.requireNonNull(tokenizer);
    }

    @Override
    public Set<InetSocketAddress> addresses() {
        return addresses;
    }

    @Override
    public void addAddress(InetSocketAddress address) {
        this.addresses.add(address);
    }

    @Override
    public void removeAddress(InetSocketAddress address) {
        this.addresses.remove(address);
    }
}
