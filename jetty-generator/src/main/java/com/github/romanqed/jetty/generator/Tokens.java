package com.github.romanqed.jetty.generator;

final class Tokens {
    static final byte COLON = (byte) ':';
    static final byte TAB = 0x09;
    static final byte LINE_FEED = 0x0A;
    static final byte CARRIAGE_RETURN = 0x0D;
    static final byte SPACE = 0x20;
    static final byte[] CRLF = {CARRIAGE_RETURN, LINE_FEED};
    private Tokens() {
    }
}
