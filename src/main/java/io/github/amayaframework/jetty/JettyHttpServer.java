package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerConfig;
import jakarta.servlet.ServletContext;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;

final class JettyHttpServer implements HttpServer {
    private final Server server;
    private final AddressSet addresses;
    private final JettyHttpConfig config;
    private final JettyServlet servlet;
    private final ServletContext context;
    private Runnable1<HttpContext> runnable;

    JettyHttpServer(Server server,
                    AddressSet addresses,
                    JettyHttpConfig config,
                    JettyServlet servlet,
                    ServletContext context) {
        this.server = server;
        this.addresses = addresses;
        this.config = config;
        this.servlet = servlet;
        this.context = context;
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
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public void bind(InetSocketAddress address, HttpVersion version) {
        addresses.add(address, version);
    }

    @Override
    public void bind(int port, HttpVersion version) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("Illegal port: " + port);
        }
        addresses.add(new InetSocketAddress(port), version);
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
        servlet.version = config.version;
        servlet.tokenizer = config.tokenizer;
        servlet.parser = config.parser;
        servlet.formatter = config.formatter;
        servlet.handler = runnable;
        server.start();
    }

    @Override
    public void stop() throws Throwable {
        server.stop();
        server.join();
    }
}
