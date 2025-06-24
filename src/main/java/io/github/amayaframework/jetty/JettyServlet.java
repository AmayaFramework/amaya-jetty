package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee9.nested.Request;
import org.eclipse.jetty.ee9.nested.Response;

import java.io.IOException;

final class JettyServlet implements Servlet {
    private final HttpCodeBuffer buffer;
    Runnable1<HttpContext> handler;
    HttpVersion version;
    PathTokenizer tokenizer;
    MimeParser parser;
    MimeFormatter formatter;
    private ServletConfig config;

    JettyServlet(HttpCodeBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void init(ServletConfig config) {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    private void handle(Request request, Response response) throws Throwable {
        // Because there are no request handling, we must set baseRequest.setHandled(false)
        if (handler == null) {
            request.setHandled(false);
            return;
        }
        // Get jetty http version
        var jettyVersion = request.getHttpVersion();
        if (jettyVersion == null) {
            response.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "Unknown http version");
            request.setHandled(true);
            return;
        }
        // Parse and check http version
        var version = HttpVersion.of(jettyVersion.getVersion());
        if (version == null || version.after(this.version)) {
            response.sendError(
                    HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED,
                    "Version " + jettyVersion + " not supported"
            );
            request.setHandled(true);
            return;
        }
        // Create amaya request
        var amayaRequest = new JettyRequest(request, version, tokenizer, parser);
        // Parse and check http method
        var method = amayaRequest.getMethod();
        if (method == null || !method.isSupported(version)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown http method");
            request.setHandled(true);
            return;
        }
        // Create amaya response
        var protocol = jettyVersion.toString();
        var scheme = request.getScheme();
        var amayaResponse = new JettyResponse(response, protocol, scheme, version, formatter);
        // Create wrapped servlet entities
        var wrappedRequest = new WrappedHttpRequest(request, amayaRequest);
        var wrappedResponse = new WrappedHttpResponse(response, amayaResponse, version, buffer, parser);
        // Create context
        var context = new JettyHttpContext(amayaRequest, amayaResponse, wrappedRequest, wrappedResponse);
        // Run handler for context
        handler.run(context);
        // Mark request as handled
        request.setHandled(true);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        try {
            handle((Request) request, (Response) response);
        } catch (Error | RuntimeException | IOException | ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException("Amaya Jetty integration servlet failed", e);
        }
    }

    @Override
    public String getServletInfo() {
        return "Amaya Jetty integration for Jetty 12 (module: amaya-jetty)";
    }

    @Override
    public void destroy() {
    }
}
