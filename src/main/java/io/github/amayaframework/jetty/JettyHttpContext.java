package io.github.amayaframework.jetty;

import io.github.amayaframework.context.AbstractContext;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.context.HttpRequest;
import io.github.amayaframework.context.HttpResponse;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class JettyHttpContext extends AbstractContext<HttpServletRequest, HttpServletResponse> implements HttpContext {
    // Amaya context
    private final JettyRequest request;
    private final JettyResponse response;
    // Http version, code buffer and parser for wrapped context
    private final HttpVersion version;
    private final HttpCodeBuffer buffer;
    private final MimeParser parser;
    // Wrapped context
    private HttpServletRequest wrappedRequest;
    private HttpServletResponse wrappedResponse;

    JettyHttpContext(HttpServletRequest originalRequest,
                     HttpServletResponse originalResponse,
                     JettyRequest request,
                     JettyResponse response,
                     HttpVersion version,
                     HttpCodeBuffer buffer,
                     MimeParser parser) {
        super(originalRequest, originalResponse);
        // Amaya context
        this.request = request;
        this.response = response;
        // Wrapped context
        this.wrappedRequest = null;
        this.wrappedResponse = null;
        this.version = version;
        this.buffer = buffer;
        this.parser = parser;
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    @Override
    public HttpServletRequest servletRequest() {
        if (wrappedRequest != null) {
            return wrappedRequest;
        }
        wrappedRequest = new WrappedHttpRequest(originalRequest, request);
        return wrappedRequest;
    }

    @Override
    public HttpResponse response() {
        return response;
    }

    @Override
    public HttpServletResponse servletResponse() {
        if (wrappedResponse != null) {
            return wrappedResponse;
        }
        wrappedResponse = new WrappedHttpResponse(originalResponse, response, version, buffer, parser);
        return wrappedResponse;
    }
}
