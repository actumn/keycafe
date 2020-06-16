package io.keycafe.server;

import io.keycafe.server.cluster.ClusterLink;
import io.keycafe.server.cluster.ClusterMsg;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.cluster.ClusterState;
import io.keycafe.server.cluster.handler.ClusterMessageHandler;
import io.keycafe.server.services.*;
import io.keycafe.server.slot.LocalSlot;
import io.keycafe.server.utils.BitmapUtils;
import io.keycafe.server.utils.RandomUtils;
import io.keycafe.server.utils.SystemInfo;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
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


    public Server(Configuration config, Map<String, String> options) {
        final InetAddress inetAddress = SystemInfo.defaultNonLoopbackIpV4Address();
        final String ipAddressOrHostname = inetAddress != null ? inetAddress.getHostAddress() : "localhost";

        this.myself = new ClusterNode(
                RandomUtils.getRandomHex(NODE_NAMELEN),
                ipAddressOrHostname,
                config.getServicePort(),
                config.getClusterPort());
        this.clusterState = new ClusterState(this.myself);
        this.lslot = new LocalSlot(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());


        this.coordination = new CoordinationService(new CoordinationServiceHandler(this), options.get("COORDINATOR"));
        this.cluster = new ClusterService(this, config.getClusterPort());
        this.slot = new SlotService(lslot, clusterState, myself, config.getServicePort());
    }

    @Override
    public void run() throws Exception {
        logger.info("Server[{}] start running", myself.getNodeId());

        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::cron, 1, 1, TimeUnit.SECONDS);

        coordination.run();
        coordination.registerClusterNode(myself.getNodeId(), myself);
        cluster.run();
        slot.run();
    }

    @Override
    public void close() {
        coordination.close();
        cluster.close();
        slot.close();
    }

    public void meet(ClusterNode clusterNode) {
        if (clusterNode.getNodeId().equals(myself.getNodeId()))
            return;

        ClusterLink link = new ClusterLink();
        link.connect(clusterNode.getHostAddress(), clusterNode.getCport(), new ClusterMessageHandler(this));
        link.sendPing(myself);
        clusterNode.link(link);
        clusterState.putNode(clusterNode.getNodeId(), clusterNode);
    }

    public void rebalanceSlots(int low, int high) {
//        logger.info("clusterAddSlots slot: low {}, high {}", low, high);

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
    }

    public void clusterProcessPacket(ChannelHandlerContext ctx, ClusterMsg msg) {
        logger.info("clusterProcessPacket - {}", msg.getType().toString());
        ClusterNode sender = clusterState.lookupNode(msg.getSender());

        if (msg.getType() == ClusterMsg.ClusterMessageType.PING) {
            ctx.writeAndFlush(new ClusterMsg(ClusterMsg.ClusterMessageType.PONG,
                    myself.getMyslots(),
                    myself.getNodeId()));
        }

        if (msg.getType() == ClusterMsg.ClusterMessageType.PING || msg.getType() == ClusterMsg.ClusterMessageType.PONG) {
            if (Arrays.equals(msg.getMyslots(), sender.getMyslots()))
                return;

            for (int i = 0; i < CLUSTER_SLOTS; i++) {
                if (BitmapUtils.bitmapTestBit(msg.getMyslots(), i)) {
                    if (clusterState.getNodeBySlot(i) == sender)
                        continue;

                    if (clusterState.getNodeBySlot(i) != null)
                        clusterDelSlot(clusterState.getNodeBySlot(i), i);

                    clusterAddSlot(sender, i);
                }
            }

//            logger.info("ping-pong result - myself: {}", Arrays.toString(myself.getMyslots()));
//            logger.info("ping-pong result - sender: {}", Arrays.toString(sender.getMyslots()));
        }
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
//        logger.info("cron start");
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

    private long iteration = 0;
    private void clusterCron() {
        iteration += 1;
        if (iteration % 100 == 0) {
            for (Map.Entry<String, ClusterNode> nodeEntry : clusterState.getNodeMap().entrySet()) {
                ClusterNode node = nodeEntry.getValue();
                if (node == myself) continue;

                node.getLink().sendPing(myself);
            }
        }
    }
}
