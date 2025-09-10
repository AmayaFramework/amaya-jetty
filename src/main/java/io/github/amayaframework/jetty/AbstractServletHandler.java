package io.github.amayaframework.jetty;

import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

abstract class AbstractServletHandler implements ServletHandler {
    protected final HttpMethodBuffer methodBuffer;
    protected final HttpCodeBuffer codeBuffer;
    protected final HttpVersion version;
    protected final HttpErrorHandler errorHandler;
    protected final PathTokenizer tokenizer;
    protected final MimeParser parser;
    protected final MimeFormatter formatter;

    protected AbstractServletHandler(HttpMethodBuffer methodBuffer,
                                     HttpCodeBuffer codeBuffer,
                                     HttpVersion version,
                                     HttpErrorHandler errorHandler,
                                     PathTokenizer tokenizer,
                                     MimeParser parser,
                                     MimeFormatter formatter) {
        this.methodBuffer = methodBuffer;
        this.codeBuffer = codeBuffer;
        this.version = version;
        this.errorHandler = errorHandler;
        this.tokenizer = tokenizer;
        this.parser = parser;
        this.formatter = formatter;
    }

    protected HttpContext buildContext(HttpServletRequest req, HttpServletResponse res) throws Throwable {
        // Get raw http version
        var rawVersion = req.getProtocol();
        if (rawVersion == null) {
            errorHandler.handle(res, HttpCode.HTTP_VERSION_NOT_SUPPORTED, "Unknown http version");
            return null;
        }
        // Parse and check an http version
        var version = HttpVersion.of(rawVersion);
        if (version == null || version.after(this.version)) {
            errorHandler.handle(res, HttpCode.HTTP_VERSION_NOT_SUPPORTED, "Version " + rawVersion + " not supported");
            return null;
        }
        // Parse and check http method
        var method = methodBuffer.get(req.getMethod());
        if (method == null || !method.isSupported(version)) {
            errorHandler.handle(res, HttpCode.BAD_REQUEST, "Unknown http method");
            return null;
        }
        // Create context
        return new JettyHttpContext(
                req,
                res,
                new JettyRequest(req, version, method, tokenizer, parser),
                new ServerHttpResponse(res, errorHandler, parser, formatter, codeBuffer, version, rawVersion, req.getScheme())
        );
    }
}
