package io.github.amayaframework.jetty;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An immutable representation of an SSL configuration.
 * <p>
 * This class provides utility factory methods to create instances with various parameter combinations.
 * <br>
 * The default keystore type is {@code PKCS12}.
 */
public final class SSLConfig {
    private static final String DEFAULT_TYPE = "PKCS12";
    private final Path path;
    private final Consumer<SslContextFactory> consumer;

    /**
     * Constructs an {@link SSLConfig} with the specified path and ssl configurer.
     *
     * @param path     the path to the keystore file, must be non-null
     * @param consumer the specified {@link SslContextFactory} configurer, must be non-null
     */
    public SSLConfig(Path path, Consumer<SslContextFactory> consumer) {
        this.path = path;
        this.consumer = consumer;
    }

    /**
     * Creates a new {@link SSLConfig} instance with the specified type, path, and password.
     * All parameters are required and must not be null.
     *
     * @param type     the type of the keystore
     * @param path     the path to the keystore file
     * @param password the password to access the keystore
     * @return a new {@link SSLConfig} instance
     */
    public static SSLConfig of(String type, Path path, String password) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(path);
        Objects.requireNonNull(password);
        return new SSLConfig(path, factory -> {
            factory.setKeyStoreType(type);
            factory.setKeyStorePassword(password);
        });
    }

    /**
     * Creates a new {@link SSLConfig} instance with the default type and specified path and password.
     * All parameters are required and must not be null.
     *
     * @param path     the path to the keystore file
     * @param password the password to access the keystore
     * @return a new {@link SSLConfig} instance
     */
    public static SSLConfig of(Path path, String password) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(password);
        return new SSLConfig(path, factory -> {
            factory.setKeyStoreType(DEFAULT_TYPE);
            factory.setKeyStorePassword(password);
        });
    }

    /**
     * Creates a new {@link SSLConfig} instance using a string path, specified type, and password.
     * All parameters are required and must not be null.
     *
     * @param type     the type of the keystore
     * @param path     the path to the keystore file as a string
     * @param password the password to access the keystore
     * @return a new {@link SSLConfig} instance
     */
    public static SSLConfig of(String type, String path, String password) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(path);
        Objects.requireNonNull(password);
        return new SSLConfig(Path.of(path), factory -> {
            factory.setKeyStoreType(type);
            factory.setKeyStorePassword(password);
        });
    }

    /**
     * Creates a new {@link SSLConfig} instance using a string path and password with default type.
     * All parameters are required and must not be null.
     *
     * @param path     the path to the keystore file as a string
     * @param password the password to access the keystore
     * @return a new {@link SSLConfig} instance
     */
    public static SSLConfig of(String path, String password) {
        Objects.requireNonNull(path);
        Objects.requireNonNull(password);
        return new SSLConfig(Path.of(path), factory -> {
            factory.setKeyStoreType(DEFAULT_TYPE);
            factory.setKeyStorePassword(password);
        });
    }

    /**
     * Returns the path to the keystore file.
     *
     * @return the path to the keystore file
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the configurer of the {@link SslContextFactory}.
     *
     * @return the configurer of the {@link SslContextFactory}
     */
    public Consumer<SslContextFactory> getConfigurer() {
        return consumer;
    }
}
