package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerConfig;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;

final class JettyHttpServer implements HttpServer {
    private final Server server;
    private final AddressSet addresses;
    private final JettyHttpConfig config;
    private final JettyHandler handler;
    private Runnable1<HttpContext> runnable;

    JettyHttpServer(Server server, AddressSet addresses, JettyHttpConfig config, JettyHandler handler) {
        this.server = server;
        this.addresses = addresses;
        this.config = config;
        this.handler = handler;
    }

    @Override
    public void bind(InetSocketAddress address) {
        addresses.add(address);
    }

    @Override
    public void bind(int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Illegal port: " + port);
        }
        addresses.add(new InetSocketAddress(port));
    }

    @Override
    public HttpServerConfig getConfig() {
        return config;
    }

    @Override
    public Runnable1<HttpContext> getHandler() {
        return runnable;
    }

    @Override
    public void setHandler(Runnable1<HttpContext> handler) {
        this.runnable = handler;
    }

    @Override
    public void start() throws Throwable {
        if (!server.isStopped()) {
            throw new IllegalStateException("Cannot start not stopped server");
        }
        handler.setVersion(config.version);
        handler.setTokenizer(config.tokenizer);
        handler.setParser(config.parser);
        handler.setFormatter(config.formatter);
        handler.setHandler(runnable);
        server.start();
    }

    @Override
    public void stop() throws Throwable {
        server.stop();
    }
}
