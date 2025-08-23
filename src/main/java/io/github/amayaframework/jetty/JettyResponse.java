package io.github.amayaframework.jetty;

import io.github.amayaframework.context.AbstractHttpResponse;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.http.MimeData;
import io.github.amayaframework.server.MimeFormatter;
import io.github.amayaframework.server.MimeParser;
import jakarta.servlet.http.HttpServletResponse;

final class JettyResponse extends AbstractHttpResponse {
    private final MimeParser parser;
    private final MimeFormatter formatter;
    private final HttpCodeBuffer codeBuffer;

    JettyResponse(HttpServletResponse response,
                  MimeParser parser,
                  MimeFormatter formatter,
                  HttpCodeBuffer codeBuffer,
                  HttpVersion version,
                  String protocol,
                  String scheme) {
        super(response, version, protocol, scheme);
        this.parser = parser;
        this.formatter = formatter;
        this.codeBuffer = codeBuffer;
    }

    @Override
    protected HttpCode parseHttpCode(int code) {
        return codeBuffer.get(code);
    }

    @Override
    protected MimeData parseMimeData(String data) {
        return parser.read(data);
    }

    @Override
    protected String formatMimeData(MimeData data) {
        return formatter.format(data);
    }
}
