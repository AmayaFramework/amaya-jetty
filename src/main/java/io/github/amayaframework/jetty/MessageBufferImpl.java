package io.github.amayaframework.jetty;

import com.github.romanqed.jetty.generator.HttpMessageBuffer;

final class MessageBufferImpl implements HttpMessageBuffer {
    private final HttpCodeBuffer buffer;

    MessageBufferImpl(HttpCodeBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public String get(int code) {
        var found = buffer.get(code);
        if (found == null) {
            return null;
        }
        return found.getDescription();
    }
}
