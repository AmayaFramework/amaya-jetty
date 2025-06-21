package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnection;

public final class StrictHttpConnection extends HttpConnection {

    public StrictHttpConnection(HttpConfiguration config,
                                Connector connector,
                                EndPoint endPoint,
                                boolean recordComplianceViolations) {
        super(config, connector, endPoint, recordComplianceViolations);
    }

    @Override
    protected HttpGenerator newHttpGenerator() {
        var config = getHttpConfiguration();
        return new StrictHttpGenerator(config.getSendServerVersion(), config.getSendXPoweredBy());
    }
}
