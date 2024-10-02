package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.server.MimeParser;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

final class WrappedHttpResponse implements HttpServletResponse {
    private final HttpServletResponse servletResponse;
    private final JettyResponse response;
    private final MimeParser parser;

    WrappedHttpResponse(HttpServletResponse servletResponse, JettyResponse response, MimeParser parser) {
        this.servletResponse = servletResponse;
        this.response = response;
        this.parser = parser;
    }

    // Method updates also amaya response

    @Override
    public void addCookie(Cookie cookie) {
        response.setCookie(cookie);
    }

    @Override
    public void setStatus(int sc) {
        servletResponse.setStatus(sc);
        response.updateStatus(HttpCode.of(sc));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setStatus(int sc, String sm) {
        servletResponse.setStatus(sc, sm);
        response.updateStatus(HttpCode.of(sc));
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        servletResponse.sendError(sc, msg);
        response.updateStatus(HttpCode.of(sc));
    }

    @Override
    public void sendError(int sc) throws IOException {
        servletResponse.sendError(sc);
        response.updateStatus(HttpCode.of(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        Objects.requireNonNull(location);
        servletResponse.sendRedirect(location);
        response.updateStatus(HttpCode.FOUND);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        servletResponse.setCharacterEncoding(charset);
        response.updateCharset(Charset.forName(charset));
    }

    @Override
    public void setContentLength(int len) {
        servletResponse.setContentLength(len);
        response.updateContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        servletResponse.setContentLengthLong(len);
        response.updateContentLength(len);
    }

    @Override
    public void setContentType(String type) {
        servletResponse.setContentType(type);
        response.updateMimeData(parser.read(type));
    }

    // Plain wrap methods

    @Override
    public boolean containsHeader(String name) {
        return servletResponse.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return servletResponse.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return servletResponse.encodeRedirectURL(url);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String encodeUrl(String url) {
        return servletResponse.encodeUrl(url);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String encodeRedirectUrl(String url) {
        return servletResponse.encodeRedirectUrl(url);
    }

    @Override
    public void setDateHeader(String name, long date) {
        servletResponse.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        servletResponse.setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        servletResponse.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        servletResponse.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        servletResponse.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        servletResponse.addIntHeader(name, value);
    }

    @Override
    public int getStatus() {
        return servletResponse.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return servletResponse.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return servletResponse.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return servletResponse.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return servletResponse.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return servletResponse.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return servletResponse.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return servletResponse.getWriter();
    }

    @Override
    public void setBufferSize(int size) {
        servletResponse.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return servletResponse.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        servletResponse.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        servletResponse.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return servletResponse.isCommitted();
    }

    @Override
    public void reset() {
        servletResponse.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        servletResponse.setLocale(loc);
    }

    @Override
    public Locale getLocale() {
        return servletResponse.getLocale();
    }
}
