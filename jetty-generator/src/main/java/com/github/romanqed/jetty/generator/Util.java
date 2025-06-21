package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpStatus;

import java.lang.reflect.Field;

final class Util {
    private Util() {
    }

    @SuppressWarnings("unchecked")
    static <T> T getConstant(String name) {
        try {
            var field = HttpGenerator.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static PreparedResponse[] getPreparedResponsesSafely() {
        try {
            return getPreparedResponses();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static PreparedResponse[] getPreparedResponses() throws Exception {
        // offset = +1 -100
        // -100 because of first http code offset
        // +1 for add 511 status
        var ret = new PreparedResponse[HttpStatus.MAX_CODE - 99];
        var objects = getInternalResponses();
        // Prepare fields
        var type = objects.getClass().getComponentType();
        var reason = type.getDeclaredField("_reason");
        var scheme = type.getDeclaredField("_schemeCode");
        var line = type.getDeclaredField("_responseLine");
        reason.setAccessible(true);
        scheme.setAccessible(true);
        line.setAccessible(true);
        for (var i = 100; i < HttpStatus.MAX_CODE + 1; ++i) {
            var object = objects[i];
            if (object == null) {
                continue;
            }
            ret[i - 100] = of(object, reason, scheme, line);
        }
        return ret;
    }

    static Object[] getInternalResponses() throws Exception {
        var field = HttpGenerator.class.getDeclaredField("__preprepared");
        field.setAccessible(true);
        return (Object[]) field.get(null);
    }

    static PreparedResponse of(Object response, Field reason, Field scheme, Field line) throws Exception {
        return new PreparedResponse(
                (byte[]) reason.get(response),
                (byte[]) scheme.get(response),
                (byte[]) line.get(response)
        );
    }
}
