package io.keycafe.server;

import io.keycafe.server.cluster.ClusterLink;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.cluster.ClusterState;
import io.keycafe.server.services.*;
import io.keycafe.server.slot.LocalSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements Service {
    public static final int CLUSTER_SLOTS = 16384;
    public static final int NODE_NAMELEN = 40;
    public static final int EXPIRE_MS = 3600000;
    private static final Logger logger = LogManager.getLogger(Server.class);

    private final SlotService slot;
    private final ClusterService cluster;
    private final CoordinationService coordination;

    private final ClusterState clusterState;
    private final ClusterNode myself;

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


        this.coordination = new CoordinationService(new CoordinationServiceHandler(this));
        this.cluster = new ClusterService(config.getClusterPort());
        this.slot = new SlotService(config.getServicePort(), lslot);
    }

    @Override
    public void run() throws Exception {
        logger.info("Server[{}] start running", myself.getNodeId());

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::cron, 1, 1, TimeUnit.SECONDS);

        coordination.run();
        coordination.registerClusterNode(myself.getNodeId(), myself);

        cluster.run();

//        slot.run();
    }

    @Override
    public void close() {
        coordination.close();
        cluster.close();
        slot.close();
    }

    public void connect(ClusterNode clusterNode) {
        if (clusterNode.getNodeId().equals(myself.getNodeId()))
            return;

        ClusterLink link = new ClusterLink(clusterNode);
        link.connect(clusterNode.getHostAddress(), clusterNode.getPort());
        link.sendPing(myself);
        clusterNode.link(link);
        clusterState.putNode(clusterNode.getNodeId(), clusterNode);
    }

    public void rebalanceSlots(int low, int high) {
        logger.info("clusterAddSlots slot: low {}, high {}", low, high);

        for (int i = 0; i < CLUSTER_SLOTS; i++) {
            if (low <= i && i < high) {
                if (myself.bitmapTestBit(i)) continue;

                clusterAddSlot(myself, i);
            }

            if (i < low || high <= i) {
                if (!myself.bitmapTestBit(i)) continue;

                clusterDelSlot(myself, i);
            }
        }
        System.out.println(Arrays.toString(myself.getMyslots()));
    }

    private void clusterAddSlot(ClusterNode node, int slot) {
        node.bitmapSetBit(slot);
        clusterState.setSlot(node, slot);
    }

    private void clusterDelSlot(ClusterNode node, int slot) {
        node.bitmapClearBit(slot);
        clusterState.setSlot(null, slot);
    }


    public void cron() {
        logger.info("cron start");
//        expireCron();
        clusterCron();
//        logger.info("cron end");
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
        for (Map.Entry<String, ClusterNode> nodeEntry : clusterState.getNodeMap().entrySet()) {
            ClusterNode node = nodeEntry.getValue();
            if (node == myself) continue;

            node.getLink().sendPing(myself);
        }
    }
}
