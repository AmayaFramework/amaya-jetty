package io.github.amayaframework.jetty;

import io.github.amayaframework.context.AbstractContext;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.context.HttpRequest;
import io.github.amayaframework.context.HttpResponse;
import io.github.amayaframework.server.ServerHttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class JettyHttpContext extends AbstractContext<HttpServletRequest, HttpServletResponse> implements HttpContext {
    // Amaya context
    private final JettyRequest request;
    private final ServerHttpResponse response;

    JettyHttpContext(HttpServletRequest servletRequest,
                     HttpServletResponse servletResponse,
                     JettyRequest request,
                     ServerHttpResponse response) {
        super(servletRequest, servletResponse);
        // Amaya context
        this.request = request;
        this.response = response;
    }

    @Override
    public HttpRequest request() {
        return request;
    }

    @Override
    public HttpResponse response() {
        return response;
    }
}
