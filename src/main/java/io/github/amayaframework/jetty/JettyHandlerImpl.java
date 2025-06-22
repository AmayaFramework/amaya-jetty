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
    private final HttpCodeBuffer buffer;
    Runnable1<HttpContext> handler;
    HttpVersion version;
    PathTokenizer tokenizer;
    MimeParser parser;
    MimeFormatter formatter;

    JettyHandlerImpl(HttpCodeBuffer buffer) {
        this.buffer = buffer;
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
        // Create amaya request and response entities
        var amayaRequest = new JettyRequest(request, version, tokenizer, parser);
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
}
