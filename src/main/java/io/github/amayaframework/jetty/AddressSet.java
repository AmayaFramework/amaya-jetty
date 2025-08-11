package io.github.amayaframework.jetty;

import com.github.romanqed.jfunc.Exceptions;
import io.github.amayaframework.http.HttpVersion;
import io.github.amayaframework.options.OptionSet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.*;

final class AddressSet implements Set<InetSocketAddress> {
    // Jetty server instance
    private final Server server;
    // Provided env root
    private final Path root;
    // Provided option set
    private final OptionSet options;
    // Content map and it sets
    private final Map<InetSocketAddress, Connector> connectors;
    private final Set<InetSocketAddress> keys;
    private final Set<Map.Entry<InetSocketAddress, Connector>> entries;

    // Current http version
    HttpVersion version;

    AddressSet(Server server, Path root, OptionSet options) {
        this.server = server;
        this.root = root;
        this.options = options;
        this.connectors = new HashMap<>();
        this.keys = connectors.keySet();
        this.entries = connectors.entrySet();
    }

    private Connector of(InetSocketAddress address, HttpVersion version) {
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
        if (version.before(HttpVersion.HTTP_1_0)) {
            throw new IllegalArgumentException("Only versions starting with HTTP/1.0 are supported");
        }
        if (version.after(this.version)) {
            throw new IllegalArgumentException("Maximum supported http version is " + this.version);
        }
        if (connectors.containsKey(address)) {
            return;
        }
        var connector = of(address, version);
        server.addConnector(connector);
        connectors.put(address, connector);
    }

    @Override
    public boolean add(InetSocketAddress address) {
        Objects.requireNonNull(address);
        if (connectors.containsKey(address)) {
            return false;
        }
        var connector = of(address, version);
        server.addConnector(connector);
        connectors.put(address, connector);
        return true;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean remove(Object o) {
        Objects.requireNonNull(o);
        if (!connectors.containsKey(o)) {
            return false;
        }
        var connector = connectors.remove(o);
        server.removeConnector(connector);
        return true;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean containsAll(Collection<?> c) {
        for (var item : c) {
            if (!connectors.containsKey(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends InetSocketAddress> c) {
        Objects.requireNonNull(c);
        var ret = false;
        for (var address : c) {
            ret |= add(address);
        }
        return ret;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        var ret = false;
        for (var address : keys) {
            if (!c.contains(address)) {
                remove(address);
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
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
        return keys.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return keys.toArray(a);
    }

    @Override
    public Iterator<InetSocketAddress> iterator() {
        return new AddressIterator(entries.iterator());
    }

    @Override
    public Spliterator<InetSocketAddress> spliterator() {
        return keys.spliterator();
    }

    @Override
    public String toString() {
        return keys.toString();
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
            server.removeConnector(current.getValue());
            iterator.remove();
            current = null;
        }
    }
}
