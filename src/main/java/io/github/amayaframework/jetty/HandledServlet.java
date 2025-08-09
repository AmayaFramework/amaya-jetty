package io.github.amayaframework.jetty;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class HandledServlet implements Servlet {
    private ServletConfig config;
    ServletHandler handler;

    @Override
    public void init(ServletConfig config) {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public String getServletInfo() {
        return "Amaya Jetty integration for Jetty 12 (module: amaya-jetty)";
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            handler.handle((HttpServletRequest) req, (HttpServletResponse) res);
        } catch (Error | RuntimeException | IOException | ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException("Amaya Jetty integration servlet failed", e);
        }
    }
}
