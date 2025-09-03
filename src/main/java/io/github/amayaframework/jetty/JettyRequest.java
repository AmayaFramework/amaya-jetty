package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpMethod;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import io.github.amayaframework.server.ServerHttpRequest;
import jakarta.servlet.http.HttpServletRequest;

final class JettyRequest extends ServerHttpRequest {
    JettyRequest(HttpServletRequest request,
                 HttpVersion version,
                 HttpMethod method,
                 PathTokenizer tokenizer,
                 MimeParser parser) {
        super(request, version, null, tokenizer, parser);
        this.method = method;
    }

    @Override
    public HttpMethod method() {
        return method;
    }
}
