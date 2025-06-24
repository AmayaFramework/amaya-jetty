package io.github.amayaframework.jetty;

import io.github.amayaframework.context.UnsupportedHttpDefinition;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeParser;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee9.nested.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

final class WrappedHttpResponse implements HttpServletResponse {
    private final Response jettyResponse;
    private final HttpVersion version;
    private final HttpCodeBuffer buffer;
    private final JettyResponse response;
    private final MimeParser parser;

    WrappedHttpResponse(Response jettyResponse,
                        JettyResponse response,
                        HttpVersion version,
                        HttpCodeBuffer buffer,
                        MimeParser parser) {
        this.jettyResponse = jettyResponse;
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
    @SuppressWarnings("deprecation")
    public void setStatus(int sc, String sm) {
        setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        var code = buffer.get(sc);
        if (code != null && !code.isSupported(version)) {
            throw new UnsupportedHttpDefinition(version, code);
        }
        jettyResponse.sendError(sc, msg);
        response.updateStatus(code);
    }

    @Override
    public void sendError(int sc) throws IOException {
        var code = buffer.get(sc);
        if (code != null && !code.isSupported(version)) {
            throw new UnsupportedHttpDefinition(version, code);
        }
        jettyResponse.sendError(sc);
        response.updateStatus(code);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        Objects.requireNonNull(location);
        jettyResponse.sendRedirect(location);
        response.updateStatus(HttpCode.FOUND);
    }

    @Override
    public void setContentLength(int len) {
        jettyResponse.setContentLength(len);
        response.updateContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        jettyResponse.setContentLengthLong(len);
        response.updateContentLength(len);
    }

    @Override
    public boolean containsHeader(String name) {
        return jettyResponse.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return jettyResponse.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return jettyResponse.encodeRedirectURL(url);
    }

    // Plain wrap methods

    @Override
    @SuppressWarnings("deprecation")
    public String encodeUrl(String url) {
        return jettyResponse.encodeUrl(url);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String encodeRedirectUrl(String url) {
        return jettyResponse.encodeRedirectUrl(url);
    }

    @Override
    public void setDateHeader(String name, long date) {
        jettyResponse.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        jettyResponse.setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        jettyResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        jettyResponse.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        jettyResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        jettyResponse.addIntHeader(name, value);
    }

    @Override
    public int getStatus() {
        return jettyResponse.getStatus();
    }

    @Override
    public void setStatus(int sc) {
        var code = buffer.get(sc);
        if (code != null && !code.isSupported(version)) {
            throw new UnsupportedHttpDefinition(version, code);
        }
        jettyResponse.setStatus(sc);
        response.updateStatus(code);
    }

    @Override
    public String getHeader(String name) {
        return jettyResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return jettyResponse.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return jettyResponse.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return jettyResponse.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (charset == null) {
            jettyResponse.setCharacterEncoding(null);
            response.updateCharset(StandardCharsets.ISO_8859_1);
            return;
        }
        jettyResponse.setCharacterEncoding(charset);
        response.updateCharset(Charset.forName(charset));
    }

    @Override
    public String getContentType() {
        return jettyResponse.getContentType();
    }

    @Override
    public void setContentType(String type) {
        if (type == null) {
            jettyResponse.setContentType(null);
            response.updateMimeData(null);
            return;
        }
        var data = parser.read(type);
        jettyResponse.setContentType(type);
        response.updateMimeData(data);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return jettyResponse.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return jettyResponse.getWriter();
    }

    @Override
    public int getBufferSize() {
        return jettyResponse.getBufferSize();
    }

    @Override
    public void setBufferSize(int size) {
        jettyResponse.setBufferSize(size);
    }

    @Override
    public void flushBuffer() throws IOException {
        jettyResponse.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        jettyResponse.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return jettyResponse.isCommitted();
    }

    @Override
    public void reset() {
        jettyResponse.reset();
    }

    @Override
    public Locale getLocale() {
        return jettyResponse.getLocale();
    }

    @Override
    public void setLocale(Locale loc) {
        jettyResponse.setLocale(loc);
    }
}
