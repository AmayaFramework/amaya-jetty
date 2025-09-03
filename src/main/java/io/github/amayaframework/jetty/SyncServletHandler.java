package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class SyncServletHandler extends AbstractServletHandler {
    private final Runnable1<HttpContext> handler;

    SyncServletHandler(HttpMethodBuffer methodBuffer,
                       HttpCodeBuffer codeBuffer,
                       HttpVersion version,
                       PathTokenizer tokenizer,
                       MimeParser parser,
                       MimeFormatter formatter,
                       Runnable1<HttpContext> handler) {
        super(methodBuffer, codeBuffer, version, tokenizer, parser, formatter);
        this.handler = handler;
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res) throws Throwable {
        var context = buildContext(req, res);
        if (context != null) {
            handler.run(context);
        }
    }
}
