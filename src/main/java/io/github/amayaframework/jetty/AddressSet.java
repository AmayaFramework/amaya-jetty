package io.github.amayaframework.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.net.InetSocketAddress;
import java.util.*;

final class AddressSet implements Set<InetSocketAddress> {
    private final Server server;
    private final Map<InetSocketAddress, Connector> connectors;
    private final Set<InetSocketAddress> keys;
    private final Set<Map.Entry<InetSocketAddress, Connector>> entries;

    AddressSet(Server server) {
        this.server = server;
        this.connectors = new HashMap<>();
        this.keys = connectors.keySet();
        this.entries = connectors.entrySet();
    }

    private Connector of(InetSocketAddress address) {
        var ret = new ServerConnector(server);
        ret.setHost(address.getHostString());
        ret.setPort(address.getPort());
        return ret;
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
    public boolean add(InetSocketAddress address) {
        Objects.requireNonNull(address);
        if (connectors.containsKey(address)) {
            return false;
        }
        var connector = of(address);
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
    public Iterator<InetSocketAddress> iterator() {
        return new AddressIterator(entries.iterator());
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
