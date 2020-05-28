package io.keycafe.server.cluster.handler;

import io.keycafe.server.Server;
import io.keycafe.server.cluster.ClusterMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusterMessageHandler extends SimpleChannelInboundHandler<ClusterMsg> {
    private static final Logger logger = LogManager.getLogger(ClusterMessageHandler.class);

    private final Server server;

    public ClusterMessageHandler(Server server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClusterMsg msg) throws Exception {
        logger.debug("receive message from {}", msg.getSender());

        server.clusterProcessPacket(ctx, msg);
    }
}
