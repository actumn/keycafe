package io.keycafe.server.cluster;

import io.netty.channel.socket.SocketChannel;

public class ClusterLink {
    private final SocketChannel channel;

    public ClusterLink(SocketChannel channel) {
        this.channel = channel;
    }
}
