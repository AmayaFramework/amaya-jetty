package io.github.amayaframework.jetty;

import io.github.amayaframework.http.MimeData;
import io.github.amayaframework.http.MimeType;
import io.github.amayaframework.server.IllegalMimeType;
import io.github.amayaframework.server.MimeParser;

final class JettyMimeParser implements MimeParser {

    private static MimeType parseType(String type) {
        var ret = MimeType.of(type);
        if (ret != null) {
            return ret;
        }
        // Find index if '/', if not found, throw bad request
        var index = type.indexOf('/');
        if (index < 0) {
            throw new IllegalMimeType(type);
        }
        var group = type.substring(0, index).strip();
        var name = type.substring(index + 1).strip();
        if (group.isEmpty() || name.isEmpty()) {
            throw new IllegalMimeType(type);
        }
        return new MimeType(group, name, null);
    }

    @Override
    public MimeData read(String s) {
        // Find index of ';'
        var index = s.indexOf(';');
        // If not found, parse only mime type
        if (index < 0) {
            return new MimeData(parseType(s));
        }
        // Otherwise split by type and parameter, then parse parameter
        var type = parseType(s.substring(0, index));
        var parameter = s.substring(index + 1);
        if (parameter.isEmpty()) {
            return new MimeData(type, null, null);
        }
        // Find index of '='
        index = parameter.indexOf('=');
        // If not found, treat as only-key parameter
        if (index < 0) {
            return new MimeData(type, parameter, null);
        }
        // Otherwise split by key and value
        var key = parameter.substring(0, index);
        var value = parameter.substring(index + 1);
        return new MimeData(type, key.strip(), value.strip());
    }
}
