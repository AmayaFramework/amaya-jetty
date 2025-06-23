package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.HttpServerConfig;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.ServletContext;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;

final class JettyHttpConfig implements HttpServerConfig {
    private static final MimeFormatter DEFAULT_FORMATTER = new JettyMimeFormatter();
    private static final MimeParser DEFAULT_PARSER = new JettyMimeParser();
    private static final PathTokenizer DEFAULT_TOKENIZER = new JettyPathTokenizer();

    final AddressSet addresses;
    final ServletContext context;
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
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public HttpVersion getHttpVersion() {
        return version;
    }

    @Override
    public void setHttpVersion(HttpVersion version) {
        Objects.requireNonNull(version);
        if (version.before(HttpVersion.HTTP_1_0)) {
            throw new IllegalArgumentException("Only versions starting with HTTP/1.0 are supported");
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
    public MimeFormatter getMimeFormatter() {
        return formatter;
    }

    @Override
    public void setMimeFormatter(MimeFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
    }

    @Override
    public MimeParser getMimeParser() {
        return parser;
    }

    @Override
    public void setMimeParser(MimeParser parser) {
        this.parser = Objects.requireNonNull(parser);
    }

    @Override
    public PathTokenizer getPathTokenizer() {
        return tokenizer;
    }

    @Override
    public void setPathTokenizer(PathTokenizer tokenizer) {
        this.tokenizer = Objects.requireNonNull(tokenizer);
    }

    @Override
    public Set<InetSocketAddress> getAddresses() {
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
