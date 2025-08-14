package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Exceptions;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

final class AddressSet implements Set<InetSocketAddress> {
    // Jetty server instance
    private final Server server;
    // Provided env root
    private final Path root;
    // Provided option set
    private final OptionSet options;
    // Content map and it sets
    private final Map<InetSocketAddress, Connector> connectors;

    // Current http version
    HttpVersion version;

    AddressSet(Server server, Path root, OptionSet options) {
        this.server = server;
        this.root = root;
        this.options = options;
        this.connectors = new HashMap<>();
    }

    private Connector createConnector(InetSocketAddress address, HttpVersion version) {
        try {
            var factory = JettyProtocols.getConnectorFactory(version);
            return factory.create(server, address, root, options);
        } catch (Throwable e) {
            Exceptions.throwAny(e);
            // Unreachable code to suppress javac error
            return null;
        }
    }

    void add(InetSocketAddress address, HttpVersion version) {
        Objects.requireNonNull(address);
        if (version.before(HttpVersion.HTTP_1_1)) {
            throw new IllegalArgumentException("Minimal allowed version is HTTP/1.1");
        }
        if (version.after(this.version)) {
            throw new IllegalArgumentException("Maximum allowed http version is " + this.version);
        }
        if (connectors.containsKey(address)) {
            return;
        }
        var connector = createConnector(address, version);
        server.addConnector(connector);
        connectors.put(address, connector);
    }

    @Override
    public boolean add(InetSocketAddress address) {
        Objects.requireNonNull(address);
        if (connectors.containsKey(address)) {
            return false;
        }
        var connector = createConnector(address, version);
        server.addConnector(connector);
        connectors.put(address, connector);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        var connector = connectors.remove(o);
        if (connector == null) {
            return false;
        }
        server.removeConnector(connector);
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return connectors.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends InetSocketAddress> c) {
        var ret = false;
        for (var address : c) {
            ret |= add(address);
        }
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean ret = false;
        var iterator = connectors.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var address = entry.getKey();
            if (!c.contains(address)) {
                iterator.remove();
                server.removeConnector(entry.getValue());
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        var ret = false;
        for (var address : c) {
            ret |= remove(address);
        }
        return ret;
    }

    @Override
    public void clear() {
        for (var connector : connectors.values()) {
            server.removeConnector(connector);
        }
        connectors.clear();
    }

    @Override
    public int size() {
        return connectors.size();
    }

    @Override
    public boolean isEmpty() {
        return connectors.isEmpty();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean contains(Object o) {
        return connectors.containsKey(o);
    }

    @Override
    public Object[] toArray() {
        return connectors.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return connectors.keySet().toArray(a);
    }

    @Override
    public Iterator<InetSocketAddress> iterator() {
        return new AddressIterator(connectors.entrySet().iterator());
    }

    @Override
    public Spliterator<InetSocketAddress> spliterator() {
        return connectors.keySet().spliterator();
    }

    @Override
    public void forEach(Consumer<? super InetSocketAddress> action) {
        connectors.keySet().forEach(action);
    }

    @Override
    public String toString() {
        return connectors.keySet().toString();
    }

    private final class AddressIterator implements Iterator<InetSocketAddress> {
        private final Iterator<Map.Entry<InetSocketAddress, Connector>> iterator;
        private Map.Entry<InetSocketAddress, Connector> current;

        private AddressIterator(Iterator<Map.Entry<InetSocketAddress, Connector>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public InetSocketAddress next() {
            var ret = iterator.next();
            this.current = ret;
            return ret.getKey();
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            var connector = current.getValue();
            iterator.remove();
            current = null;
            server.removeConnector(connector);
        }
    }
}
