package io.github.amayaframework.jetty;

import com.github.romanqed.jsync.AsyncRunnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class AsyncServletHandler extends AbstractServletHandler {
    private final AsyncRunnable1<HttpContext> handler;

    AsyncServletHandler(HttpMethodBuffer methodBuffer,
                        HttpCodeBuffer codeBuffer,
                        HttpVersion version,
                        PathTokenizer tokenizer,
                        MimeParser parser,
                        MimeFormatter formatter,
                        AsyncRunnable1<HttpContext> handler) {
        super(methodBuffer, codeBuffer, version, tokenizer, parser, formatter);
        this.handler = handler;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res) throws Throwable {
        var asyncCtx = req.startAsync();
        HttpContext context;
        try {
            context = buildContext(
                    (HttpServletRequest) asyncCtx.getRequest(),
                    (HttpServletResponse) asyncCtx.getResponse()
            );
            if (context == null) {
                asyncCtx.complete();
                return;
            }
        } catch (Throwable e) {
            asyncCtx.complete();
            throw e;
        }
        handler.runAsync(context).whenComplete((v, t) -> asyncCtx.complete());
    }
}
