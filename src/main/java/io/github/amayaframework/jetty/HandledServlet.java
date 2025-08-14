package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable0;
import com.github.romanqed.jfunc.Runnable1;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class HandledServlet implements Servlet {
    private ServletConfig config;
    ServletHandler handler;
    Runnable1<ServletConfig> onInit;
    Runnable0 onDestroy;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        try {
            if (onInit != null) {
                onInit.run(config);
            }
        } catch (Error | RuntimeException | ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException("Failed to initialize amaya servlet", e);
        }
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
        try {
            if (onDestroy != null) {
                onDestroy.run();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
