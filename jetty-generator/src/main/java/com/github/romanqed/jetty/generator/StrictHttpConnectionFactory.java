//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

// Copyright 2024 Roman Bakaldin
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.romanqed.jetty.generator;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;

import java.util.Objects;

/**
 * A Connection Factory for HTTP Connections.
 * <p>Accepts connections either directly or via SSL and/or ALPN chained connection factories. The accepted
 * {@link StrictHttpConnection}s are configured by a {@link HttpConfiguration} instance that is either created by
 * default or passed in to the constructor.
 */
public class StrictHttpConnectionFactory
        extends AbstractConnectionFactory
        implements HttpConfiguration.ConnectionFactory {
    private final HttpConfiguration config;
    private final HttpMessageBuffer buffer;
    private boolean useInputDirectByteBuffers;
    private boolean useOutputDirectByteBuffers;

    public StrictHttpConnectionFactory(HttpConfiguration config, HttpMessageBuffer buffer) {
        super(HttpVersion.HTTP_1_1.asString());
        this.config = Objects.requireNonNull(config);
        installBean(config);
        this.buffer = buffer;
        this.useInputDirectByteBuffers = config.isUseInputDirectByteBuffers();
        this.useOutputDirectByteBuffers = config.isUseOutputDirectByteBuffers();
    }

    public StrictHttpConnectionFactory(HttpConfiguration config) {
        this(config, null);
    }

    public StrictHttpConnectionFactory(HttpMessageBuffer buffer) {
        this(new HttpConfiguration(), buffer);
    }

    public StrictHttpConnectionFactory() {
        this(new HttpConfiguration(), null);
    }

    @Override
    public HttpConfiguration getHttpConfiguration() {
        return config;
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
        var connection = new StrictHttpConnection(config, connector, endPoint, buffer);
        connection._useInputDirectByteBuffers = useInputDirectByteBuffers;
        connection._useOutputDirectByteBuffers = useOutputDirectByteBuffers;
        return configure(connection, connector, endPoint);
    }
}
