package io.github.amayaframework.jetty;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class Main {
    public static void main(String[] args) throws Exception {
        var server = new Server(8080);
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(String target,
                               Request baseRequest,
                               HttpServletRequest request,
                               HttpServletResponse response) {
                var tok = new PathTokenizerImpl();
                var t = tok.tokenize(baseRequest.getRequestURI());
                System.out.println(t);
                baseRequest.setHandled(true);
            }
        });
        server.start();
    }
}
