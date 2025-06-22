package io.github.amayaframework.jetty.servlet;

import io.github.amayaframework.jetty.JettyHandler;
import jakarta.servlet.*;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.io.IOException;

final class JettyServlet implements Servlet {
    private final JettyHandler handler;
    private ServletConfig config;

    JettyServlet(JettyHandler handler) {
        this.handler = handler;
    }

    @Override
    public void init(ServletConfig config) {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        try {
            handler.handle((Request) request, (Response) response);
        } catch (Error | RuntimeException | IOException | ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException("Amaya Jetty integration servlet failed", e);
        }
    }

    @Override
    public String getServletInfo() {
        return "Amaya Jetty integration for Jetty 11 (module: amaya-jetty)";
    }

    @Override
    public void destroy() {
    }
}
