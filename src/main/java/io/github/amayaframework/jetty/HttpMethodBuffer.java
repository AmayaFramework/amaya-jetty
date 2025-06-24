package io.github.amayaframework.jetty;

import io.github.amayaframework.http.HttpMethod;

/**
 * An interface describing an abstract buffer of {@link HttpMethod}.
 */
public interface HttpMethodBuffer {

    /**
     * Gets an {@link HttpMethod} instance assigned with given string.
     *
     * @param method the specified http method name
     * @return the {@link HttpMethod} instance if found, null otherwise
     */
    HttpMethod get(String method);
}
