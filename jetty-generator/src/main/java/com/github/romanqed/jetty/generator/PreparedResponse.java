package com.github.romanqed.jetty.generator;

final class PreparedResponse {
    final String reason;
    final byte[] scheme;
    final byte[] line;

    PreparedResponse(String reason, byte[] scheme, byte[] line) {
        this.reason = reason;
        this.scheme = scheme;
        this.line = line;
    }
}
