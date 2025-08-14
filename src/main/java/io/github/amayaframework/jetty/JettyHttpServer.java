package io.github.amayaframework.jetty;

import com.github.romanqed.jct.CancelToken;
import com.github.romanqed.juni.UniRunnable1;
import io.github.amayaframework.context.HttpContext;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.server.HttpServer;
import io.github.amayaframework.server.HttpServerConfig;
import io.github.amayaframework.service.AbstractService;
import io.github.amayaframework.service.ServiceCallback;
import jakarta.servlet.ServletContext;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;

final class JettyHttpServer extends AbstractService implements HttpServer {
    private final Server server;
    private final AddressSet addresses;
    private final JettyHttpConfig config;
    private final HttpMethodBuffer methodBuffer;
    private final HttpCodeBuffer codeBuffer;
    private final boolean preferAsync;
    private final HandledServlet servlet;
    private final ServletContext context;
    private UniRunnable1<HttpContext> runnable;

    JettyHttpServer(Server server,
                    AddressSet addresses,
                    JettyHttpConfig config,
                    ServletContext context,
                    HttpMethodBuffer methodBuffer,
                    HttpCodeBuffer codeBuffer,
                    boolean preferAsync,
                    HandledServlet servlet) {
        this.server = server;
        this.addresses = addresses;
        this.config = config;
        this.context = context;
        this.methodBuffer = methodBuffer;
        this.codeBuffer = codeBuffer;
        this.preferAsync = preferAsync;
        this.servlet = servlet;
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
    public ServletContext servletContext() {
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
    public HttpServerConfig config() {
        return config;
    }

    @Override
    public UniRunnable1<HttpContext> handler() {
        return runnable;
    }

    @Override
    public void handler(UniRunnable1<HttpContext> handler) {
        this.runnable = handler;
    }

    private ServletHandler createSyncHandler() {
        return new SyncServletHandler(
                methodBuffer,
                codeBuffer,
                config.version,
                config.tokenizer,
                config.parser,
                config.formatter,
                runnable
        );
    }

    private ServletHandler createAsyncHandler() {
        return new AsyncServletHandler(
                methodBuffer,
                codeBuffer,
                config.version,
                config.tokenizer,
                config.parser,
                config.formatter,
                runnable
        );
    }

    private ServletHandler createHandler() {
        if (runnable == null) {
            return (req, res) -> {};
        }
        // 1. isSync() => run()
        // 2. isAsync() => runAsync()
        // 3. isUni() / unknown => preferAsync ? runAsync() : run()
        if (runnable.isSync()) {
            return createSyncHandler();
        }
        if (runnable.isAsync()) {
            return createAsyncHandler();
        }
        return preferAsync ? createAsyncHandler() : createSyncHandler();
    }

    @Override
    protected void doStart(CancelToken token, ServiceCallback callback) throws Throwable {
        if (token.canceled()) {
            return;
        }
        servlet.handler = createHandler();
        servlet.onInit = config.onInit;
        servlet.onDestroy = config.onDestroy;
        server.start();
    }

    @Override
    protected void doStop(CancelToken token) throws Throwable {
        if (token.canceled()) {
            return;
        }
        server.stop();
        if (!token.canceled()) {
            server.join();
        }
    }

    @Override
    protected void doDispose() {
        try {
            server.stop();
            server.destroy();
        } catch (Exception ignored) {
            // No exceptions while disposing
        }
    }
}
