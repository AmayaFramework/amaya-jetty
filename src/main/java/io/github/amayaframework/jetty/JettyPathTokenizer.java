package io.github.amayaframework.jetty;

import io.github.amayaframework.server.PathTokenizer;

import java.util.*;

final class JettyPathTokenizer implements PathTokenizer {
    private static final List<String> SINGLE = List.of("/");

    @Override
    public List<String> tokenize(String s) {
        if (s.equals("/")) {
            return SINGLE;
        }
        var ret = new ArrayList<String>();
        var tokenizer = new StringTokenizer(s, "/");
        while (tokenizer.hasMoreTokens()) {
            ret.add(tokenizer.nextToken());
        }
        return ret;
    }
}
