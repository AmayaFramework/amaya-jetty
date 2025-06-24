package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

final class JettyHandlerImpl implements JettyHandler {
    private final HttpMethodBuffer methodBuffer;
    private final HttpCodeBuffer codeBuffer;
    Runnable1<HttpContext> handler;
    HttpVersion version;
    PathTokenizer tokenizer;
    MimeParser parser;
    MimeFormatter formatter;

    JettyHandlerImpl(HttpMethodBuffer methodBuffer, HttpCodeBuffer codeBuffer) {
        this.methodBuffer = methodBuffer;
        this.codeBuffer = codeBuffer;
    }

    @Override
    public void handle(Request request, Response response) throws Throwable {
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
        var amayaRequest = new JettyRequest(request, version, methodBuffer, tokenizer, parser);
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
        var wrappedResponse = new WrappedHttpResponse(response, amayaResponse, version, codeBuffer, parser);
        // Create context
        var context = new JettyHttpContext(amayaRequest, amayaResponse, wrappedRequest, wrappedResponse);
        // Run handler for context
        handler.run(context);
        // Mark request as handled
        request.setHandled(true);
    }
}
