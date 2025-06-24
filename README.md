# amaya-jetty [![amaya-jetty](https://img.shields.io/maven-central/v/io.github.amayaframework/amaya-jetty?color=blue)](https://repo1.maven.org/maven2/io/github/amayaframework/amaya-jetty)
The amaya-server implementation is based on jetty-server.

## Getting Started

To install it, you will need:

* Java 11+
* Maven/Gradle

### Features

* Full implementation of amaya-server
* Convenient interfaces for http request handling
* Full access to jetty configuration

## Installing

### Gradle dependency

```Groovy
dependencies {
    implementation group: 'io.github.amayaframework', name: 'amaya-jetty', version: '1.2.2-11'
    // For alpn support (for ssl + http2, optionally)
    implementation group: 'org.eclipse.jetty', name: 'jetty-alpn-server', version: '11.0.25'
    implementation group: 'org.eclipse.jetty', name: 'jetty-alpn-java-server', version: '11.0.25'
    // For http2 support (optionally)
    implementation group: 'org.eclipse.jetty.http2', name: 'http2-server', version: '11.0.25'
    // For http3 support (optionally)
    implementation group: 'org.eclipse.jetty.http3', name: 'http3-server', version: '11.0.25'
}
```

### Maven dependency

```
<dependencies>
    <dependency>
        <groupId>io.github.amayaframework</groupId>
        <artifactId>amaya-jetty</artifactId>
        <version>1.2.2-11</version>
    </dependency>

    <!-- For ALPN support (for SSL + HTTP/2, optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-alpn-server</artifactId>
        <version>11.0.25</version>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jetty</groupId>
        <artifactId>jetty-alpn-java-server</artifactId>
        <version>11.0.25</version>
    </dependency>

    <!-- For HTTP/2 support (optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty.http2</groupId>
        <artifactId>http2-server</artifactId>
        <version>11.0.25</version>
    </dependency>

    <!-- For HTTP/3 support (optionally) -->
    <dependency>
        <groupId>org.eclipse.jetty.http3</groupId>
        <artifactId>http3-server</artifactId>
        <version>11.0.25</version>
    </dependency>
</dependencies>
```

## Examples

### Hello world

```Java
import io.github.amayaframework.jetty.JettyServerFactory;

public class Main {
    public static void main(String[] args) throws Throwable {
        var factory = new JettyServerFactory();
        var server = factory.create();
        server.setHandler(ctx -> {
            var req = ctx.getRequest();
            var rsp = ctx.getResponse();
            rsp.getWriter().write("Hello, " + req.getQueryParameter("user"));
        });
        server.bind(8080);
        server.start();
    }
}
```

### Custom thread pool (virtual threads)

```Java
import io.github.amayaframework.jetty.JettyServerFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Throwable {
        var factory = new JettyServerFactory(() -> {
            var ret = new QueuedThreadPool();
            ret.setVirtualThreadsExecutor(Executors.newVirtualThreadPerTaskExecutor());
            return ret;
        });
        var server = factory.create();
        server.setHandler(ctx -> {
            var req = ctx.getRequest();
            var rsp = ctx.getResponse();
            rsp.getWriter().write("Hello, " + req.getQueryParameter("user"));
        });
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
        server.setHandler(ctx -> {
            var req = ctx.getRequest();
            var rsp = ctx.getResponse();
            rsp.getWriter().write("Hello, " + req.getQueryParameter("user"));
        });
        server.bind(8080);
        server.start();
    }
}
```

## Built With

* [Gradle](https://gradle.org) - Dependency management
* [Jetty 11](https://jetty.org/docs/jetty/11/index.html) - Http server implementation
* [jfunc](https://github.com/RomanQed/jfunc) - Basic functional interfaces
* [amaya-context](https://github.com/AmayaFramework/amaya-core) - Universal http context api
* [amaya-server](https://github.com/AmayaFramework/amaya-core) - Universal server api

## Authors

* **[RomanQed](https://github.com/RomanQed)** - *Main work*

See also the list of [contributors](https://github.com/AmayaFramework/amaya-jetty/contributors)
who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details
