package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.StringUtil;

import java.nio.ByteBuffer;

final class Util {
    static final byte[] COLON_SPACE = new byte[]{':', ' '};
    static final byte[] CONTENT_LENGTH_0 = StringUtil.getBytes("Content-Length: 0\r\n");

    private Util() {
    }

    static void putSanitisedValue(String value, ByteBuffer buffer) {
        int length = value.length();
        for (var i = 0; i < length; ++i) {
            var c = value.charAt(i);
            if (c > 0xff || c == '\r' || c == '\n') {
                buffer.put((byte) ' ');
            } else {
                buffer.put((byte) (0xff & c));
            }
        }
    }

    static void putSanitisedName(String name, ByteBuffer buffer) {
        int length = name.length();
        for (var i = 0; i < length; ++i) {
            var c = name.charAt(i);
            if (c > 0xff || c == '\r' || c == '\n' || c == ':') {
                buffer.put((byte) '?');
            } else {
                buffer.put((byte) (0xff & c));
            }
        }
    }

    static void putTo(HttpField field, ByteBuffer bufferInFillMode) {
        if (field instanceof PreEncodedHttpField) {
            ((PreEncodedHttpField) field).putTo(bufferInFillMode, HttpVersion.HTTP_1_0);
            return;
        }
        if (field == null) {
            return;
        }
        var header = field.getHeader();
        if (header != null) {
            bufferInFillMode.put(header.getBytesColonSpace());
            putSanitisedValue(field.getValue(), bufferInFillMode);
        } else {
            putSanitisedName(field.getName(), bufferInFillMode);
            bufferInFillMode.put(COLON_SPACE);
            putSanitisedValue(field.getValue(), bufferInFillMode);
        }
        BufferUtil.putCRLF(bufferInFillMode);
    }

    static void putContentLength(ByteBuffer header, long contentLength) {
        if (contentLength == 0) {
            header.put(CONTENT_LENGTH_0);
        } else {
            header.put(HttpHeader.CONTENT_LENGTH.getBytesColonSpace());
            BufferUtil.putDecLong(header, contentLength);
            header.put(Tokens.CRLF);
        }
    }
}
