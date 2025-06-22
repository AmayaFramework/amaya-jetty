package io.github.amayaframework.jetty.servlet;

import io.github.amayaframework.jetty.JettyHandler;
import io.github.amayaframework.jetty.JettyHandlerConfigurer;
import io.github.amayaframework.jetty.JettyOptions;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

final class ServletHandlerConfigurer implements JettyHandlerConfigurer {
    private final JettyContextHandlerConfigurer configurer;

    ServletHandlerConfigurer(JettyContextHandlerConfigurer configurer) {
        this.configurer = configurer;
    }

    private void configure(ServletContextHandler handler, OptionSet options) {
        if (configurer == null) {
            return;
        }
        if (options == null) {
            configurer.configure(handler);
        } else {
            configurer.configure(handler, options);
        }
    }

    private Handler createHandler(JettyHandler handler, OptionSet options) {
        var ret = new ServletContextHandler();
        ret.addServlet(new ServletHolder(new JettyServlet(handler)), "/");
        if (options != null && options.asKey(JettyOptions.ENABLE_SESSIONS)) {
            ret.setSessionHandler(new SessionHandler());
        }
        configure(ret, options);
        return ret;
    }

    @Override
    public void configure(Server server, JettyHandler handler, OptionSet options) {
        var servletHandler = createHandler(handler, options);
        server.setHandler(servletHandler);
    }

    @Override
    public void configure(Server server, JettyHandler handler) {
        var servletHandler = createHandler(handler, null);
        server.setHandler(servletHandler);
    }
}
