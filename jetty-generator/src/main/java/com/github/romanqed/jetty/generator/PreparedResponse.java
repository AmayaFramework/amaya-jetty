package com.github.romanqed.jetty.generator;

final class PreparedResponse {
    final byte[] reason;
    final byte[] scheme;
    final byte[] line;

    PreparedResponse(byte[] reason, byte[] scheme, byte[] line) {
        this.reason = reason;
        this.scheme = scheme;
        this.line = line;
    }
}
