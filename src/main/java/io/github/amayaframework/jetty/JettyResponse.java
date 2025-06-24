package io.github.amayaframework.jetty;

import io.github.amayaframework.context.AbstractHttpResponse;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.http.MimeData;
import io.github.amayaframework.server.MimeFormatter;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.Charset;

final class JettyResponse extends AbstractHttpResponse {
    private final MimeFormatter formatter;

    JettyResponse(HttpServletResponse response,
                  String protocol,
                  String scheme,
                  HttpVersion version,
                  MimeFormatter formatter) {
        super(response, protocol, scheme, version);
        this.formatter = formatter;
    }

    void updateStatus(HttpCode code) {
        this.status = code;
    }

    void updateCharset(Charset charset) {
        this.charset = charset;
    }

    void updateContentLength(long length) {
        this.length = length;
    }

    void updateMimeData(MimeData data) {
        this.data = data;
    }

    @Override
    protected String formatMimeData(MimeData mimeData) {
        return formatter.format(mimeData);
    }
}
