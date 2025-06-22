package io.github.amayaframework.jetty;

import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;
import java.nio.file.Path;

interface ConnectorFactory {

    Connector create(Server server, InetSocketAddress address, Path root, OptionSet options) throws Throwable;
}
