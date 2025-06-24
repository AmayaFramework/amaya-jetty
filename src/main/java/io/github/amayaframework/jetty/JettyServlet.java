package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

final class JettyServlet implements Servlet {
    private final HttpMethodBuffer methodBuffer;
    private final HttpCodeBuffer codeBuffer;
    Runnable1<HttpContext> handler;
    HttpVersion version;
    PathTokenizer tokenizer;
    MimeParser parser;
    MimeFormatter formatter;
    private ServletConfig config;

    JettyServlet(HttpMethodBuffer methodBuffer, HttpCodeBuffer codeBuffer) {
        this.methodBuffer = methodBuffer;
        this.codeBuffer = codeBuffer;
    }

    @Override
    public void init(ServletConfig config) {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    private void handle(HttpServletRequest request, HttpServletResponse response) throws Throwable {
        // Because there are no request handling, we just do nothing
        if (handler == null) {
            return;
        }
        // Get raw http version
        var rawVersion = request.getProtocol();
        if (rawVersion == null) {
            response.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "Unknown http version");
            return;
        }
        // Parse and check http version
        var version = HttpVersion.of(rawVersion);
        if (version == null || version.after(this.version)) {
            response.sendError(
                    HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED,
                    "Version " + rawVersion + " not supported"
            );
            return;
        }
        // Create amaya request
        var amayaRequest = new JettyRequest(request, version, methodBuffer, tokenizer, parser);
        // Parse and check http method
        var method = amayaRequest.getMethod();
        if (method == null || !method.isSupported(version)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown http method");
            return;
        }
        // Create amaya response
        var scheme = request.getScheme();
        var amayaResponse = new JettyResponse(response, rawVersion, scheme, version, formatter);
        // Create wrapped servlet entities
        var wrappedRequest = new WrappedHttpRequest(request, amayaRequest);
        var wrappedResponse = new WrappedHttpResponse(response, amayaResponse, version, codeBuffer, parser);
        // Create context
        var context = new JettyHttpContext(amayaRequest, amayaResponse, wrappedRequest, wrappedResponse);
        // Run handler for context
        handler.run(context);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        try {
            handle((HttpServletRequest) request, (HttpServletResponse) response);
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
