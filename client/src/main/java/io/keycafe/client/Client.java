package io.keycafe.client;

import io.keycafe.client.network.Connection;
import io.keycafe.client.util.StringCodec;
import io.keycafe.common.Protocol;

import java.io.IOException;

import static io.keycafe.common.Protocol.KEYCAFE_CHARSET;

public class Client extends Connection implements Commands {
    public Client() {
        super();
    }

    public Client(final String host) {
        super(host);
    }

    public Client(final String host, final int port) {
        super(host, port);
    }

    @Override
    public void get(String key) {
        this.sendCommand(Protocol.Command.GET, StringCodec.encode(key));
    }

    @Override
    public void set(String key, String value) {
        this.sendCommand(Protocol.Command.SET, StringCodec.encode(key), StringCodec.encode(value));
    }

    @Override
    public void delete(String key) {
        this.sendCommand(Protocol.Command.DELETE, StringCodec.encode(key));
    }

    @Override
    public void clusterSlots() {
        this.sendCommand(Protocol.Command.CLUSTER, StringCodec.encode("slots"));
    }
}
