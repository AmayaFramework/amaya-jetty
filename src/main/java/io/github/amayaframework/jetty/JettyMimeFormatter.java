package io.github.amayaframework.jetty;

import io.github.amayaframework.http.MimeData;
import io.github.amayaframework.server.MimeFormatter;

final class JettyMimeFormatter implements MimeFormatter {

    @Override
    public String format(MimeData mimeData) {
        return mimeData.toString();
    }
}
