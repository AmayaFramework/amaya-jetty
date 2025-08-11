package io.github.amayaframework.jetty;

import io.github.amayaframework.context.UnsupportedHttpDefinition;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeParser;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

final class WrappedHttpResponse implements HttpServletResponse {
    private final HttpServletResponse original;
    private final HttpVersion version;
    private final HttpCodeBuffer buffer;
    private final JettyResponse response;
    private final MimeParser parser;

    WrappedHttpResponse(HttpServletResponse original,
                        JettyResponse response,
                        HttpVersion version,
                        HttpCodeBuffer buffer,
                        MimeParser parser) {
        this.original = original;
        this.response = response;
        this.version = version;
        this.buffer = buffer;
        this.parser = parser;
    }

    // Method updates also amaya response

    @Override
    public void addCookie(Cookie cookie) {
        response.setCookie(cookie);
    }

    @Override
    public void setStatus(int sc) {
        var code = buffer.get(sc);
        if (code == null) {
            original.setStatus(sc);
            response.updateStatus(new HttpCode(sc, null, version));
        } else {
            if (!code.isSupported(version)) {
                throw new UnsupportedHttpDefinition(version, code);
            }
            original.setStatus(sc);
            response.updateStatus(code);
        }
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        var code = buffer.get(sc);
        if (code == null) {
            original.sendError(sc, msg);
            response.updateStatus(new HttpCode(sc, null, version));
        } else {
            if (!code.isSupported(version)) {
                throw new UnsupportedHttpDefinition(version, code);
            }
            original.sendError(sc, msg);
            response.updateStatus(code);
        }
    }

    @Override
    public void sendError(int sc) throws IOException {
        var code = buffer.get(sc);
        if (code == null) {
            original.sendError(sc);
            response.updateStatus(new HttpCode(sc, null, version));
        } else {
            if (!code.isSupported(version)) {
                throw new UnsupportedHttpDefinition(version, code);
            }
            original.sendError(sc);
            response.updateStatus(code);
        }
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        Objects.requireNonNull(location);
        original.sendRedirect(location);
        response.updateStatus(HttpCode.FOUND);
    }

    @Override
    public void setContentLength(int len) {
        original.setContentLength(len);
        response.updateContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        original.setContentLengthLong(len);
        response.updateContentLength(len);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (charset == null) {
            original.setCharacterEncoding(null);
            response.updateCharset(StandardCharsets.ISO_8859_1);
            return;
        }
        original.setCharacterEncoding(charset);
        response.updateCharset(Charset.forName(charset));
    }

    @Override
    public void setContentType(String type) {
        if (type == null) {
            original.setContentType(null);
            response.updateMimeData(null);
            return;
        }
        var data = parser.read(type);
        original.setContentType(type);
        response.updateMimeData(data);
    }

    // Methods with additional checks

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        if (supplier == null) {
            return;
        }
        if (version.before(HttpVersion.HTTP_1_1)) {
            throw new IllegalStateException("Trailers not supported in " + version);
        }
        original.setTrailerFields(supplier);
    }

    // Plain wrap methods

    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        return original.getTrailerFields();
    }

    @Override
    public boolean containsHeader(String name) {
        return original.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return original.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return original.encodeRedirectURL(url);
    }

    @Override
    public void setDateHeader(String name, long date) {
        original.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        original.setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        original.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        original.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        original.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        original.addIntHeader(name, value);
    }

    @Override
    public int getStatus() {
        return original.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return original.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return original.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return original.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return original.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return original.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return original.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return original.getWriter();
    }

    @Override
    public int getBufferSize() {
        return original.getBufferSize();
    }

    @Override
    public void setBufferSize(int size) {
        original.setBufferSize(size);
    }

    @Override
    public void flushBuffer() throws IOException {
        original.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        original.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return original.isCommitted();
    }

    @Override
    public void reset() {
        original.reset();
    }

    @Override
    public Locale getLocale() {
        return original.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        original.setLocale(loc);
    }
}
