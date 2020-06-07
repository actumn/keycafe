package io.keycafe.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class Keycafe implements KeycafeCommands, Closeable {
    private final Client client;

    public Keycafe() {
        this.client = new Client();
    }

    public Keycafe(final String host) {
        this.client = new Client(host);
    }

    public Keycafe(final String host, final int port) {
        this.client = new Client(host, port);
    }

    @Override
    public String get(String key) {
        client.get(key);
        return client.getBulkReply();
    }

    @Override
    public String set(String key, String value) {
        client.set(key, value);
        return client.getSimpleString();
    }

    @Override
    public String delete(String key) {
        client.delete(key);
        return client.getSimpleString();
    }

    @Override
    public List<Object> clusterSlots() {
        client.clusterSlots();
        return client.getArray();
    }

    public void connect() {
        client.connect();
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void close() {
        client.close();
    }
}
