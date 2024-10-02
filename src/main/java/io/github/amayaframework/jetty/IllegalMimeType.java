package io.github.amayaframework.jetty;

public final class IllegalMimeType extends IllegalArgumentException {
    private final String type;

    public IllegalMimeType(String type) {
        super("Illegal mime type: " + type);
        this.type = type;
    }

    public String getMimeType() {
        return type;
    }
}
