package io.github.amayaframework.jetty;

import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.context.HttpRequest;
import io.github.amayaframework.context.HttpResponse;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class JettyHttpContext implements HttpContext {
    // Amaya context
    private final JettyRequest request;
    private final JettyResponse response;
    // Original context
    private final HttpServletRequest originalRequest;
    private final HttpServletResponse originalResponse;
    // Http version, code buffer and parser for wrapped context
    private final HttpVersion version;
    private final HttpCodeBuffer buffer;
    private final MimeParser parser;
    // Wrapped context
    private HttpServletRequest wrappedRequest;
    private HttpServletResponse wrappedResponse;

    JettyHttpContext(JettyRequest request,
                     JettyResponse response,
                     HttpServletRequest originalRequest,
                     HttpServletResponse originalResponse,
                     HttpVersion version,
                     HttpCodeBuffer buffer,
                     MimeParser parser) {
        // Amaya context
        this.request = request;
        this.response = response;
        // Original context
        this.originalRequest = originalRequest;
        this.originalResponse = originalResponse;
        // Wrapped context
        this.wrappedRequest = null;
        this.wrappedResponse = null;
        this.version = version;
        this.buffer = buffer;
        this.parser = parser;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletRequest getServletRequest() {
        if (wrappedRequest != null) {
            return wrappedRequest;
        }
        wrappedRequest = new WrappedHttpRequest(originalRequest, request);
        return wrappedRequest;
    }

    @Override
    public HttpServletRequest getOriginalRequest() {
        return originalRequest;
    }

    @Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public HttpServletResponse getServletResponse() {
        if (wrappedResponse != null) {
            return wrappedResponse;
        }
        wrappedResponse = new WrappedHttpResponse(originalResponse, response, version, buffer, parser);
        return wrappedResponse;
    }

    @Override
    public HttpServletResponse getOriginalResponse() {
        return originalResponse;
    }
}
