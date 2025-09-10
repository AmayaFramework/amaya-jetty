package io.github.amayaframework.jetty;

import com.github.romanqed.jsync.AsyncRunnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CancellationException;

final class AsyncServletHandler extends AbstractServletHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncServletHandler.class);
    private final AsyncRunnable1<HttpContext> handler;

    AsyncServletHandler(HttpMethodBuffer methodBuffer,
                        HttpCodeBuffer codeBuffer,
                        HttpVersion version,
                        HttpErrorHandler errorHandler,
                        PathTokenizer tokenizer,
                        MimeParser parser,
                        MimeFormatter formatter,
                        AsyncRunnable1<HttpContext> handler) {
        super(methodBuffer, codeBuffer, version, errorHandler, tokenizer, parser, formatter);
        this.handler = handler;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res) {
        var asyncCtx = req.startAsync();
        var rs = (HttpServletResponse) asyncCtx.getResponse();
        try {
            var context = buildContext((HttpServletRequest) asyncCtx.getRequest(), rs);
            if (context == null) {
                asyncCtx.complete();
                return;
            }
            handler.runAsync(context).whenComplete((v, t) -> {
                try {
                    if (t != null) {
                        if (t instanceof CancellationException) {
                            LOGGER.debug("Async request cancelled: {}", t.getMessage());
                            rs.setStatus(204); // no content
                        } else {
                            LOGGER.error("Unhandled async exception", t);
                            errorHandler.handle(rs, HttpCode.INTERNAL_SERVER_ERROR, t.getMessage());
                        }
                    }
                } catch (IOException ioe) {
                    LOGGER.error("Failed to send error response", ioe);
                } finally {
                    asyncCtx.complete();
                }
            });
        } catch (Throwable e) {
            try {
                LOGGER.error("Synchronous error in async handler", e);
                errorHandler.handle(rs, HttpCode.INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (IOException ioe) {
                LOGGER.error("Failed to send error response", ioe);
            } finally {
                asyncCtx.complete();
            }
        }
    }
}
