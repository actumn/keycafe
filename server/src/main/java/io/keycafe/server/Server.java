package io.keycafe.server;

import io.keycafe.server.cluster.ClusterConnector;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.cluster.ClusterState;
import io.keycafe.server.services.*;
import io.keycafe.server.slot.LocalSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    public static final int CLUSTER_SLOTS = 16384;
    public static final int NODE_NAMELEN = 40;
    public static final int EXPIRE_MS = 3600000;
    private static final Logger logger = LogManager.getLogger(Server.class);

    private final SlotService slot;
    private final ClusterService cluster;
    private final CoordinationService coordination;

    private final ClusterState clusterState;
    private final ClusterNode myself;
    private final ClusterConnector connector;

    private final LocalSlot lslot;


    public Server(Configuration config) {
//        final InetAddress inetAddress = SystemInfo.defaultNonLoopbackIpV4Address();
//        final String ipAddressOrHostname = inetAddress != null ? inetAddress.getHostAddress() : "localhost";

        this.myself = new ClusterNode(
                Utils.getRandomHex(NODE_NAMELEN),
                "localhost",
                config.getClusterPort());
        this.clusterState = new ClusterState(this.myself);
        this.lslot = new LocalSlot(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        this.connector = new ClusterConnector();


        this.coordination = new CoordinationService(new CoordinationServiceHandler(this));
        this.cluster = new ClusterService(config.getClusterPort());
        this.slot = new SlotService(config.getServicePort(), lslot);
    }

    public void run() throws Exception {
        logger.info("Server[{}] start running", myself.getNodeId());

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::cron, 100, 100, TimeUnit.MILLISECONDS);

        coordination.run();
        coordination.registerClusterNode(myself.getNodeId(), myself);

        cluster.run();

//        slot.run();
    }

    public void connect(ClusterNode clusterNode) {
        if (clusterNode.getNodeId().equals(myself.getNodeId()))
            return;

        clusterNode.link(connector.connect(clusterNode.getHostAddress(), clusterNode.getPort()));
        // TODO:: cluster ping
        clusterState.putNode(clusterNode.getNodeId(), clusterNode);
    }

    public void cron() {
        logger.info("cron start");
//        expireCron();
        clusterCron();
        logger.info("cron end");
    }

    private void expireCron() {
        Long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> stringLongEntry : lslot.expire.entrySet()) {
            // Remove key if 1h passes
            if (now - stringLongEntry.getValue() > EXPIRE_MS) {
                lslot.expire.remove(stringLongEntry.getKey());
            }
        }
    }

    private void clusterCron() {

    }
}
