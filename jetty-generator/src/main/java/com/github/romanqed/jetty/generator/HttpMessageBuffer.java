package com.github.romanqed.jetty.generator;

/**
 * An interface describing an abstract buffer of messages for http codes.
 * For example, "OK" for 200 etc.
 */
public interface HttpMessageBuffer {

    /**
     * Gets message for given http code.
     *
     * @param code the specified http code
     * @return message string if found, null otherwise
     */
    String get(int code);
}
