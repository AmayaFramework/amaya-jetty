package io.github.amayaframework.jetty;

import java.nio.file.Path;
import java.util.Objects;

/**
 * An immutable representation of an SSL keystore credentials, including its path, password, and type.
 * <p>
 * This class provides utility factory methods to create instances with various parameter combinations.
 * <br>
 * The default keystore type is {@code PKCS12}.
 */
public final class SSLKeystore {
    private static final String DEFAULT_TYPE = "PKCS12";
    private final String type;
    private final Path path;
    private final String password;

    /**
     * Constructs an {@link SSLKeystore} with the specified type, path, and password.
     *
     * @param type     the type of the keystore, must be non-null
     * @param path     the path to the keystore file, must be non-null
     * @param password the password to access the keystore, must be non-null
     */
    public SSLKeystore(String type, Path path, String password) {
        this.type = type;
        this.path = path;
        this.password = password;
    }

    /**
     * Constructs an {@link SSLKeystore} with the default type ({@code PKCS12}), the given path, and password.
     *
     * @param path     the path to the keystore file, must be non-null
     * @param password the password to access the keystore, must be non-null
     */
    public SSLKeystore(Path path, String password) {
        this.type = DEFAULT_TYPE;
        this.path = path;
        this.password = password;
    }

    /**
     * Creates a new {@link SSLKeystore} instance with the specified type, path, and password.
     * All parameters are required and must not be null.
     *
     * @param type     the type of the keystore
     * @param path     the path to the keystore file
     * @param password the password to access the keystore
     * @return a new {@link SSLKeystore} instance
     */
    public static SSLKeystore of(String type, Path path, String password) {
        return new SSLKeystore(
                Objects.requireNonNull(type),
                Objects.requireNonNull(path),
                Objects.requireNonNull(password)
        );
    }

    /**
     * Creates a new {@link SSLKeystore} instance with the default type and specified path and password.
     * All parameters are required and must not be null.
     *
     * @param path     the path to the keystore file
     * @param password the password to access the keystore
     * @return a new {@link SSLKeystore} instance
     */
    public static SSLKeystore of(Path path, String password) {
        return new SSLKeystore(
                DEFAULT_TYPE,
                Objects.requireNonNull(path),
                Objects.requireNonNull(password)
        );
    }

    /**
     * Creates a new {@link SSLKeystore} instance using a string path, specified type, and password.
     * All parameters are required and must not be null.
     *
     * @param type     the type of the keystore
     * @param path     the path to the keystore file as a string
     * @param password the password to access the keystore
     * @return a new {@link SSLKeystore} instance
     */
    public static SSLKeystore of(String type, String path, String password) {
        Objects.requireNonNull(path);
        return new SSLKeystore(
                Objects.requireNonNull(type),
                Path.of(path),
                Objects.requireNonNull(password)
        );
    }

    /**
     * Creates a new {@link SSLKeystore} instance using a string path and password with default type.
     * All parameters are required and must not be null.
     *
     * @param path     the path to the keystore file as a string
     * @param password the password to access the keystore
     * @return a new {@link SSLKeystore} instance
     */
    public static SSLKeystore of(String path, String password) {
        Objects.requireNonNull(path);
        return new SSLKeystore(
                DEFAULT_TYPE,
                Path.of(path),
                Objects.requireNonNull(password)
        );
    }

    /**
     * Creates a new {@link SSLKeystore} instance using the current directory as the path,
     * the default type, and the specified password.
     *
     * @param password the password to access the keystore
     * @return a new {@link SSLKeystore} instance
     */
    public static SSLKeystore of(String password) {
        Objects.requireNonNull(password);
        return new SSLKeystore(DEFAULT_TYPE, Path.of("."), password);
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
     * Returns the password used to access the keystore.
     *
     * @return the keystore password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        var that = (SSLKeystore) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "SSLKeystore{" +
                "path='" + path + '\'' +
                '}';
    }
}
