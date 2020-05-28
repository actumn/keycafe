package io.keycafe.server.cluster;

import io.keycafe.server.Server;

import java.util.Arrays;

public class ClusterNode {
    private final String nodeId;
    private final String hostAddress;
    private final int port;
    private ClusterLink link;
    private final byte[] myslots = new byte[Server.CLUSTER_SLOTS / 8];

    public ClusterNode(String nodeId, String hostAddress, int port) {
        this.nodeId = nodeId;
        this.hostAddress = hostAddress;
        this.port = port;

        Arrays.fill(this.myslots, (byte) 0);
    }

    public ClusterNodeConfig config() {
        return new ClusterNodeConfig(nodeId, hostAddress, port);
    }
    public static ClusterNode fromConfig(ClusterNodeConfig config) {
        return new ClusterNode(config.getNodeId(), config.getHostAddress(), config.getPort());
    }

    public void link(ClusterLink link) {
        this.link = link;
    }

    public ClusterLink getLink() {
        return link;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }

    public byte[] getMyslots() {
        return myslots;
    }

    public boolean bitmapTestBit(int slot) {
        // slot 은 0 ~ 16383
        // 8로 나누면 0 ~ 2047
        int pos = slot / 8;
        int bit = slot & 7;
        return (myslots[pos] & (1 << bit)) != 0;
    }

    public void bitmapSetBit(int slot) {
        // slot 은 0 ~ 16383
        // 8로 나누면 0 ~ 2047
        int pos = slot / 8;
        int bit = slot & 7;
        synchronized (myslots) {
            myslots[pos] |= (1 << bit);
        }
    }

    /* Clear the bit at position 'pos' in a bitmap. */
    public void bitmapClearBit(int slot) {
        // slot 은 0 ~ 16383
        // 8로 나누면 0 ~ 2047
        int pos = slot / 8;
        int bit = slot & 7;
        synchronized (myslots) {
            myslots[pos] &= ~(1<<bit);
        }
    }
}
