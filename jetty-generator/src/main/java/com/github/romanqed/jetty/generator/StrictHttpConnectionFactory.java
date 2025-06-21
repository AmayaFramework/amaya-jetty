package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConfiguration.ConnectionFactory;

import java.util.Objects;

public class StrictHttpConnectionFactory extends AbstractConnectionFactory implements ConnectionFactory {
    private final HttpConfiguration config;
    private boolean recordHttpComplianceViolations;
    private boolean useInputDirectByteBuffers;
    private boolean useOutputDirectByteBuffers;

    public StrictHttpConnectionFactory(HttpConfiguration config) {
        super(HttpVersion.HTTP_1_1.asString());
        this.config = Objects.requireNonNull(config);
        addBean(config);
        this.useInputDirectByteBuffers = config.isUseInputDirectByteBuffers();
        this.useOutputDirectByteBuffers = config.isUseOutputDirectByteBuffers();
    }

    public StrictHttpConnectionFactory() {
        this(new HttpConfiguration());
    }

    @Override
    public HttpConfiguration getHttpConfiguration() {
        return config;
    }

    public boolean isRecordHttpComplianceViolations() {
        return recordHttpComplianceViolations;
    }

    public void setRecordHttpComplianceViolations(boolean recordHttpComplianceViolations) {
        this.recordHttpComplianceViolations = recordHttpComplianceViolations;
    }

    public boolean isUseInputDirectByteBuffers() {
        return useInputDirectByteBuffers;
    }

    public void setUseInputDirectByteBuffers(boolean useInputDirectByteBuffers) {
        this.useInputDirectByteBuffers = useInputDirectByteBuffers;
    }

    public boolean isUseOutputDirectByteBuffers() {
        return useOutputDirectByteBuffers;
    }

    public void setUseOutputDirectByteBuffers(boolean useOutputDirectByteBuffers) {
        this.useOutputDirectByteBuffers = useOutputDirectByteBuffers;
    }

    @Override
    public Connection newConnection(Connector connector, EndPoint endPoint) {
        var connection = new StrictHttpConnection(config, connector, endPoint, recordHttpComplianceViolations);
        connection.setUseInputDirectByteBuffers(useInputDirectByteBuffers);
        connection.setUseOutputDirectByteBuffers(useOutputDirectByteBuffers);
        return configure(connection, connector, endPoint);
    }
}
