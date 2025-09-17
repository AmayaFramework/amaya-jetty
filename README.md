# amaya-jetty [![amaya-jetty](https://img.shields.io/maven-central/v/io.github.amayaframework/amaya-jetty?color=blue)](https://repo1.maven.org/maven2/io/github/amayaframework/amaya-jetty)
The amaya-server implementation is based on jetty-server.

## Getting Started

To install it, you will need:

* Java 17+
* Maven/Gradle

### Features

* Full implementation of amaya-server
* Convenient interfaces for http request handling
* Full access to jetty configuration

## Installing

### Gradle dependency

```Groovy
dependencies {
    implementation group: 'io.github.amayaframework', name: 'amaya-jetty', version: '3.3.1-12.1.1'
    // For alpn support (for ssl + http2, optionally)
    implementation group: 'org.eclipse.jetty', name: 'jetty-alpn-server', version: '12.1.1'
    implementation group: 'org.eclipse.jetty', name: 'jetty-alpn-java-server', version: '12.1.1'
    // For http2 support (optionally)
    implementation group: 'org.eclipse.jetty.http2', name: 'jetty-http2-server', version: '12.1.1'
    // For http3 support (optionally)
    implementation group: 'org.eclipse.jetty.http3', name: 'jetty-http3-server', version: '12.1.1'
    implementation group: 'org.eclipse.jetty.quic', name: 'jetty-quic-quiche-server', version: '12.1.1'
    implementation group: 'org.eclipse.jetty.quic', name: 'jetty-quic-quiche-jna', version: '12.1.1'
    // For websocket support (optionally)
    implementation group: 'org.eclipse.jetty.ee10.websocket', name: 'jetty-ee10-websocket-jakarta-server', version: '12.1.1'
}
```

### Maven dependency

```
<dependencies>
    <dependency>
        <groupId>io.github.amayaframework</groupId>
        <artifactId>amaya-jetty</artifactId>
        <version>3.3.1-12.1.1</version>
    </dependency>

    <!-- For ALPN support (for SSL + HTTP/2, optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-alpn-server</artifactId>
        <version>12.1.1</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-alpn-java-server</artifactId>
        <version>12.1.1</version>
    </dependency>

    <!-- For HTTP/2 support (optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty.http2</groupId>
        <artifactId>jetty-http2-server</artifactId>
        <version>12.1.1</version>
    </dependency>

    <!-- For HTTP/3 support (optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty.http3</groupId>
        <artifactId>jetty-http3-server</artifactId>
        <version>12.1.1</version>
    </dependency>
    
    <!-- For Websocket support (optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty.ee10.websocket</groupId>
        <artifactId>jetty-ee10-websocket-jakarta-server</artifactId>
        <version>12.1.1</version>
    </dependency>
```

## Examples

### Hello world

```Java
import io.github.amayaframework.jetty.JettyServerFactory;

public class Main {
    public static void main(String[] args) throws Throwable {
        var factory = new JettyServerFactory();
        var server = factory.create();
        server.handler(UniRunnable1.of(ctx -> {
            var req = ctx.request();
            var rsp = ctx.response();
            rsp.writer().write("Hello, " + req.queryParam("user"));
        }));
        server.bind(8080);
        server.start();
    }
}
```

### Custom jetty server instance

```Java
import io.github.amayaframework.jetty.JettyFactory;
import io.github.amayaframework.jetty.JettyServerFactory;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Server;

public class Main {
    public static void main(String[] args) throws Throwable {
        var factory = new JettyServerFactory(v -> new Server());
        var server = factory.create();
        server.handler(UniRunnable1.of(ctx -> {
            var req = ctx.request();
            var rsp = ctx.response();
            rsp.writer().write("Hello, " + req.queryParam("user"));
        }));
        server.bind(8080);
        server.start();
    }
}
```

## Built With

* [Gradle](https://gradle.org) - Dependency management
* [Jetty 12](https://jetty.org/docs/jetty/12/index.html) - Http server implementation
* [jfunc](https://github.com/RomanQed/jfunc) - Basic functional interfaces
* [amaya-context](https://github.com/AmayaFramework/amaya-core) - Universal http context api
* [amaya-server](https://github.com/AmayaFramework/amaya-core) - Universal server api

## Authors

* **[RomanQed](https://github.com/RomanQed)** - *Main work*

See also the list of [contributors](https://github.com/AmayaFramework/amaya-jetty/contributors)
who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details
