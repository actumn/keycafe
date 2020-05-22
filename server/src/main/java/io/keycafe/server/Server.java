package io.keycafe.server;

import io.keycafe.server.cluster.ClusterConnector;
import io.keycafe.server.cluster.ClusterLink;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.config.Configuration;
import io.keycafe.server.services.*;
import io.keycafe.server.slot.LocalSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    public static final int NODE_NAMELEN = 40;
    public static final int EXPIRE_MS = 3600000;
    private static final Logger logger = LogManager.getLogger(Server.class);

    private final SlotService slot;
    private ClusterService cluster;
    private ClusterChannelHandler clusterChannelHandler;
    private CoordinationService coordination;
    private CoordinationServiceHandler coordinationServiceHandler;

    private final Configuration config;
    private final ClusterNode myself;
    private final ClusterConnector connector;

    private final LocalSlot lslot = new LocalSlot(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());

    public Server(Configuration config) {
        this.config = config;

//        final InetAddress inetAddress = SystemInfo.defaultNonLoopbackIpV4Address();
//        final String ipAddressOrHostname = inetAddress != null ? inetAddress.getHostAddress() : "localhost";

        this.myself = new ClusterNode(
                Utils.getRandomHex(NODE_NAMELEN),
                "localhost",
                config.getClusterPort());


//        this.coordination = new CoordinationService(new CoordinationServiceHandler(this));
//        this.cluster = new ClusterService(config.getClusterPort());
        this.slot = new SlotService(config.getServicePort(), lslot);

        this.connector = new ClusterConnector();
    }

    public void run() throws Exception {
        logger.info("Server[{}] start running", myself.getNodeId());

//        coordination.run();
//        coordination.registerClusterNode(myself.getNodeId(), myself);

//        cluster.run();
        slot.run();
    }

    public void connect(ClusterNode clusterNode) {
        if (clusterNode.getNodeId().equals(myself.getNodeId()))
            return;

        logger.info(clusterNode.getNodeId());
        logger.info(clusterNode.getHostAddress());
        logger.info(clusterNode.getPort());

        ClusterLink link = connector.connect(clusterNode.getHostAddress(), clusterNode.getPort());
    }

    public void cron() {
        Long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> stringLongEntry : lslot.expireStore.entrySet()) {
            // Remove key if 1h passes
            if (now - stringLongEntry.getValue() > EXPIRE_MS) {
                lslot.expireStore.remove(stringLongEntry.getKey());
            }
        }
    }
}
