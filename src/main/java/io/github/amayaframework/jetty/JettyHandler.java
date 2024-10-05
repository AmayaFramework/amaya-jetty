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
        // Parse http version
    }
}
