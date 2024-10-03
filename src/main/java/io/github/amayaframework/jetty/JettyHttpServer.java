package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Runnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerConfig;

import java.net.InetSocketAddress;

final class JettyHttpServer implements HttpServer {

    @Override
    public void bind(InetSocketAddress inetSocketAddress) {

    }

    @Override
    public HttpServerConfig getConfig() {
        return null;
    }

    @Override
    public Runnable1<HttpContext> getHandler() {
        return null;
    }

    @Override
    public void setHandler(Runnable1<HttpContext> handler) {

    }

    @Override
    public void start() throws Throwable {

    }

    @Override
    public void stop() throws Throwable {

    }
}
