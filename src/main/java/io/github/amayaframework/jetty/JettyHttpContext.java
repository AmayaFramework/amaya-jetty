package io.github.amayaframework.jetty;

import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.context.HttpRequest;
import io.github.amayaframework.context.HttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class JettyHttpContext implements HttpContext {
    private final HttpRequest request;
    private final HttpResponse response;
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;

    JettyHttpContext(HttpRequest request,
                     HttpResponse response,
                     HttpServletRequest servletRequest,
                     HttpServletResponse servletResponse) {
        this.request = request;
        this.response = response;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public HttpServletResponse getServletResponse() {
        return servletResponse;
    }
}
