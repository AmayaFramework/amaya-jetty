package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.HttpVersion;

import java.util.Arrays;

final class PrepareUtil {
    // Magic constant is 8: HTTP/1.1 -> len("http") = 4, len("/1.1") = 4 -> 8
    private static final int VERSION_LENGTH = 8;

    private PrepareUtil() {
    }

    static PreparedResponse prepare(int code, String message) {
        var length = message.length();
        var line = new byte[VERSION_LENGTH + 5 + length + 2];
        // Set protocol version
        HttpVersion.HTTP_1_1.toBuffer().get(line, 0, VERSION_LENGTH);
        // Set code
        line[VERSION_LENGTH] = ' ';
        line[VERSION_LENGTH + 1] = (byte) ('0' + code / 100);
        line[VERSION_LENGTH + 2] = (byte) ('0' + (code % 100) / 10);
        line[VERSION_LENGTH + 3] = (byte) ('0' + (code % 10));
        line[VERSION_LENGTH + 4] = ' ';
        // Set message
        for (var j = 0; j < length; ++j) {
            line[VERSION_LENGTH + 5 + j] = (byte) message.charAt(j);
        }
        // Set CRLF
        line[VERSION_LENGTH + 5 + length] = Tokens.CARRIAGE_RETURN;
        line[VERSION_LENGTH + 6 + length] = Tokens.LINE_FEED;
        // Build response data
        var scheme = Arrays.copyOfRange(line, 0, VERSION_LENGTH + 5);
        return new PreparedResponse(message, scheme, line);
    }

    static PreparedResponse[] prepare() {
        // offset = +1 -100
        // -100 because of first http code offset
        // +1 for add 511 status
        var ret = new PreparedResponse[HttpStatus.MAX_CODE - 99];
        for (var i = 100; i < HttpStatus.MAX_CODE + 1; ++i) {
            var code = HttpStatus.getCode(i);
            if (code == null) {
                continue;
            }
            ret[i - 100] = prepare(i, code.getMessage());
        }
        return ret;
    }
}
