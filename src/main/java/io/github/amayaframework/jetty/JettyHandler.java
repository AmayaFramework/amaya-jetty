package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

final class JettyHandler extends AbstractHandler {
    private Runnable1<HttpContext> handler;
    private HttpVersion version;
    private PathTokenizer tokenizer;
    private MimeParser parser;
    private MimeFormatter formatter;

    void setHandler(Runnable1<HttpContext> handler) {
        this.handler = handler;
    }

    void setVersion(HttpVersion version) {
        this.version = version;
    }

    void setTokenizer(PathTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    void setParser(MimeParser parser) {
        this.parser = parser;
    }

    void setFormatter(MimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        // Skip request if handler is null
        // Because there are no request handling, we must not set baseRequest.setHandled(true)
        if (handler == null) {
            return;
        }
        // Parse and check http version
        var jettyVersion = baseRequest.getHttpVersion();
        if (jettyVersion == null) {
            response.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "Unknown http version");
            baseRequest.setHandled(true);
            return;
        }
        var version = HttpVersion.of(jettyVersion.getVersion());
        if (version == null || version.after(this.version)) {
            response.sendError(
                    HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED,
                    "Version " + jettyVersion + " not supported"
            );
            baseRequest.setHandled(true);
            return;
        }
        // Create amaya request and response entities
        var jettyRequest = new JettyRequest(request, version, tokenizer, parser);
        var protocol = jettyVersion.toString();
        var scheme = baseRequest.getScheme();
        var jettyResponse = new JettyResponse(response, protocol, scheme, version, formatter);
        // Create wrapped servlet entities
        var wrappedRequest = new WrappedHttpRequest(request, jettyRequest);
        var wrappedResponse = new WrappedHttpResponse(response, jettyResponse, version, parser);
        // Create context
        var context = new JettyHttpContext(jettyRequest, jettyResponse, wrappedRequest, wrappedResponse);
        // Try to handle request
        try {
            // Run handler for context
            handler.run(context);
            // Mark request as handled
            baseRequest.setHandled(true);
        } catch (Error | RuntimeException | IOException | ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
