package io.github.amayaframework.jetty;

import io.github.amayaframework.context.AbstractHttpRequest;
import io.github.amayaframework.http.HttpMethod;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.http.MimeData;
import io.github.amayaframework.server.MimeParser;
import io.github.amayaframework.server.PathTokenizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.Charset;
import java.util.*;

final class JettyRequest extends AbstractHttpRequest {
    private final PathTokenizer tokenizer;
    private final MimeParser parser;
    private final Map<String, Object> pathParameters;

    JettyRequest(HttpServletRequest request, HttpVersion version, PathTokenizer tokenizer, MimeParser parser) {
        super(request, version);
        this.tokenizer = tokenizer;
        this.parser = parser;
        this.pathParameters = new HashMap<>();
    }

    void updateCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    protected Map<String, Cookie> collectCookies() {
        return Collections.unmodifiableMap(super.collectCookies());
    }

    @Override
    protected HttpMethod parseHttpMethod(String s) {
        return HttpMethod.of(s);
    }

    @Override
    protected List<String> splitPath(String s) {
        return Collections.unmodifiableList(tokenizer.tokenize(s));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, List<Object>> collectQueries() {
        var query = request.getQueryString();
        if (query == null) {
            return Collections.EMPTY_MAP;
        }
        var ret = new HashMap<String, List<Object>>();
        var tokenizer = new StringTokenizer(query, "&");
        while (tokenizer.hasMoreTokens()) {
            var token = tokenizer.nextToken();
            // Try to locate '=' index
            var index = token.indexOf('=');
            // If not found, then save it as key-only parameter
            if (index < 0) {
                ret.computeIfAbsent(token.strip(), k -> new ArrayList<>());
                continue;
            }
            // If found, then save it as key-value parameter
            var key = token.substring(0, index).strip();
            var value = token.substring(index + 1);
            ret.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return ret;
    }

    @Override
    protected MimeData parseMimeData(String s) {
        return parser.read(s);
    }

    @Override
    public Map<String, Object> getPathParameters() {
        return pathParameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getPathParameter(String s) {
        return (T) pathParameters.get(s);
    }
}
