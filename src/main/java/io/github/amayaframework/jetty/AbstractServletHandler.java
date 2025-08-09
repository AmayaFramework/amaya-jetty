package io.github.amayaframework.jetty;

import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

abstract class AbstractServletHandler implements ServletHandler {
    protected final HttpMethodBuffer methodBuffer;
    protected final HttpCodeBuffer codeBuffer;
    protected final HttpVersion version;
    protected final PathTokenizer tokenizer;
    protected final MimeParser parser;
    protected final MimeFormatter formatter;

    protected AbstractServletHandler(HttpMethodBuffer methodBuffer,
                                     HttpCodeBuffer codeBuffer,
                                     HttpVersion version,
                                     PathTokenizer tokenizer,
                                     MimeParser parser,
                                     MimeFormatter formatter) {
        this.methodBuffer = methodBuffer;
        this.codeBuffer = codeBuffer;
        this.version = version;
        this.tokenizer = tokenizer;
        this.parser = parser;
        this.formatter = formatter;
    }

    protected HttpContext buildContext(HttpServletRequest req, HttpServletResponse res) throws Throwable {
        // Get raw http version
        var rawVersion = req.getProtocol();
        if (rawVersion == null) {
            res.sendError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "Unknown http version");
            return null;
        }
        // Parse and check an http version
        var version = HttpVersion.of(rawVersion);
        if (version == null || version.after(this.version)) {
            res.sendError(
                    HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED,
                    "Version " + rawVersion + " not supported"
            );
            return null;
        }
        // Parse and check http method
        var method = methodBuffer.get(req.getMethod());
        if (method == null || !method.isSupported(version)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown http method");
            return null;
        }
        // Create amaya request
        var amayaRequest = new JettyRequest(req, version, methodBuffer, tokenizer, parser);
        amayaRequest.updateHttpMethod(method);
        // Create amaya response
        var scheme = req.getScheme();
        var amayaResponse = new JettyResponse(res, rawVersion, scheme, version, formatter);
        // Create context
        return new JettyHttpContext(
                req, res, // Original context
                amayaRequest, amayaResponse, // Amaya context
                version, codeBuffer, parser // Params for wrapped context
        );
    }
}
