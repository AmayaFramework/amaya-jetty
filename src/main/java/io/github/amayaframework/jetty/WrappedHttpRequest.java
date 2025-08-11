package io.github.amayaframework.jetty;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

final class WrappedHttpRequest implements HttpServletRequest {
    private final HttpServletRequest original;
    private final JettyRequest request;

    WrappedHttpRequest(HttpServletRequest original, JettyRequest request) {
        this.original = original;
        this.request = request;
    }

    @Override
    public String getAuthType() {
        return original.getAuthType();
    }

    // Method updates also amaya request

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        original.setCharacterEncoding(s);
        request.updateCharset(Charset.forName(s));
    }

    // Plain wrap methods

    @Override
    public HttpServletMapping getHttpServletMapping() {
        return original.getHttpServletMapping();
    }

    @Override
    public PushBuilder newPushBuilder() {
        return original.newPushBuilder();
    }

    @Override
    public Map<String, String> getTrailerFields() {
        return original.getTrailerFields();
    }

    @Override
    public boolean isTrailerFieldsReady() {
        return original.isTrailerFieldsReady();
    }

    @Override
    public String getRequestId() {
        return original.getRequestId();
    }

    @Override
    public String getProtocolRequestId() {
        return original.getProtocolRequestId();
    }

    @Override
    public ServletConnection getServletConnection() {
        return original.getServletConnection();
    }

    @Override
    public Cookie[] getCookies() {
        return original.getCookies();
    }

    @Override
    public long getDateHeader(String s) {
        return original.getDateHeader(s);
    }

    @Override
    public String getHeader(String s) {
        return original.getHeader(s);
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        return original.getHeaders(s);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return original.getHeaderNames();
    }

    @Override
    public int getIntHeader(String s) {
        return original.getIntHeader(s);
    }

    @Override
    public String getMethod() {
        return original.getMethod();
    }

    @Override
    public String getPathInfo() {
        return original.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return original.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return original.getContextPath();
    }

    @Override
    public String getQueryString() {
        return original.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return original.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String s) {
        return original.isUserInRole(s);
    }

    @Override
    public Principal getUserPrincipal() {
        return original.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return original.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return original.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return original.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return original.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean b) {
        return original.getSession(b);
    }

    @Override
    public HttpSession getSession() {
        return original.getSession();
    }

    @Override
    public String changeSessionId() {
        return original.changeSessionId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return original.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return original.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return original.isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return original.authenticate(httpServletResponse);
    }

    @Override
    public void login(String s, String s1) throws ServletException {
        original.login(s, s1);
    }

    @Override
    public void logout() throws ServletException {
        original.logout();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return original.getParts();
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return original.getPart(s);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) throws IOException, ServletException {
        return original.upgrade(aClass);
    }

    @Override
    public Object getAttribute(String s) {
        return original.getAttribute(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return original.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return original.getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return original.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return original.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return original.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return original.getInputStream();
    }

    @Override
    public String getParameter(String s) {
        return original.getParameter(s);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return original.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String s) {
        return original.getParameterValues(s);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return original.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return original.getProtocol();
    }

    @Override
    public String getScheme() {
        return original.getScheme();
    }

    @Override
    public String getServerName() {
        return original.getServerName();
    }

    @Override
    public int getServerPort() {
        return original.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return original.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return original.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return original.getRemoteHost();
    }

    @Override
    public void setAttribute(String s, Object o) {
        original.setAttribute(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        original.removeAttribute(s);
    }

    @Override
    public Locale getLocale() {
        return original.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return original.getLocales();
    }

    @Override
    public boolean isSecure() {
        return original.isSecure();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return original.getRequestDispatcher(s);
    }

    @Override
    public int getRemotePort() {
        return original.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return original.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return original.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return original.getLocalPort();
    }

    @Override
    public ServletContext getServletContext() {
        return original.getServletContext();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return original.startAsync();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return original.startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return original.isAsyncStarted();
    }

    @Override
    public boolean isAsyncSupported() {
        return original.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        return original.getAsyncContext();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return original.getDispatcherType();
    }
}
