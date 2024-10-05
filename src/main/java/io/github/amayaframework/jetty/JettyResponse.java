package io.github.amayaframework.jetty;

import io.github.amayaframework.context.AbstractHttpResponse;
import io.github.amayaframework.context.UnsupportedHttpDefinition;
import io.github.amayaframework.http.HttpCode;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.http.MimeData;
import io.github.amayaframework.server.MimeFormatter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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
    public void setStatus(HttpCode code) {
        if (!code.isSupported(version)) {
            throw new UnsupportedHttpDefinition(version, code);
        }
        super.setStatus(code);
    }

    @Override
    public void sendError(HttpCode code, String message) throws IOException {
        if (!code.isSupported(version)) {
            throw new UnsupportedHttpDefinition(version, code);
        }
        super.sendError(code, message);
    }

    @Override
    public void sendError(HttpCode code) throws IOException {
        if (!code.isSupported(version)) {
            throw new UnsupportedHttpDefinition(version, code);
        }
        super.sendError(code);
    }

    @Override
    protected String formatMimeData(MimeData mimeData) {
        return formatter.format(mimeData);
    }
}
