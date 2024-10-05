package io.github.amayaframework.jetty;

import org.eclipse.jetty.server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        var server = new Server(8080);
        server.setHandler(new JettyHandler());
        server.start();
    }
}
