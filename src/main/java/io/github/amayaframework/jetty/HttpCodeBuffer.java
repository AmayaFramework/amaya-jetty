package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpCode;

/**
 * An interface describing an abstract buffer of {@link HttpCode}.
 */
public interface HttpCodeBuffer {

    /**
     * Gets an {@link HttpCode} instance assigned with given code.
     *
     * @param code the specified http code
     * @return the {@link HttpCode} instance if found, null otherwise
     */
    HttpCode get(int code);
}
