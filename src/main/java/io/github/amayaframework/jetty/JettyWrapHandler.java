package io.github.amayaframework.jetty;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

final class JettyWrapHandler extends AbstractHandler {
    private final JettyHandler handler;

    JettyWrapHandler(JettyHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        try {
            handler.handle(baseRequest, (Response) response);
        } catch (Error | RuntimeException | IOException | ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
