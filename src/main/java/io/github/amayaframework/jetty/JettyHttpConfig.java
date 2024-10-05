package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.HttpServerConfig;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;

final class JettyHttpConfig implements HttpServerConfig {
    private static final MimeFormatter DEFAULT_FORMATTER = new JettyMimeFormatter();
    private static final MimeParser DEFAULT_PARSER = new JettyMimeParser();
    private static final PathTokenizer DEFAULT_TOKENIZER = new JettyPathTokenizer();

    final Set<InetSocketAddress> addresses;
    HttpVersion version;
    PathTokenizer tokenizer;
    MimeParser parser;
    MimeFormatter formatter;

    JettyHttpConfig(Set<InetSocketAddress> addresses) {
        this.addresses = addresses;
        this.version = HttpVersion.HTTP_1_1;
        this.tokenizer = DEFAULT_TOKENIZER;
        this.parser = DEFAULT_PARSER;
        this.formatter = DEFAULT_FORMATTER;
    }

    @Override
    public HttpVersion getHttpVersion() {
        return version;
    }

    @Override
    public void setHttpVersion(HttpVersion version) {
        Objects.requireNonNull(version);
        if (version.after(HttpVersion.HTTP_1_1)) {
            throw new IllegalArgumentException("Unsupported http version: " + version);
        }
        this.version = version;
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
