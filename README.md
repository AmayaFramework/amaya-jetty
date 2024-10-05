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
    implementation group: 'com.github.romanqed', name: 'amaya-jetty', version: '1.0.0'
}
```

### Maven dependency

```
<dependency>
    <groupId>io.github.amayaframework</groupId>
    <artifactId>amaya-jetty</artifactId>
    <version>1.0.0</version>
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
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;

public class Main {
    public static void main(String[] args) throws Throwable {
        var factory = new JettyServerFactory(new MyServerFactory());
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

final class MyServerFactory implements JettyFactory {

    @Override
    public Server create(Handler handler, OptionSet options) {
        // Just ignore options for example
        return create(handler);
    }

    @Override
    public Server create(Handler handler) {
        var ret = new Server();
        var sessionHandler = new SessionHandler();
        sessionHandler.setHandler(handler);
        ret.setHandler(sessionHandler);
        return ret;
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
