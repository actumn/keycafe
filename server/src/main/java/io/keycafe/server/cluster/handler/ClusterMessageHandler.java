package io.keycafe.server.cluster.handler;

import io.keycafe.server.Server;
import io.keycafe.server.cluster.ClusterMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusterMessageHandler extends SimpleChannelInboundHandler<ClusterMessage> {
    private static final Logger logger = LogManager.getLogger(ClusterMessageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClusterMessage msg) throws Exception {
        logger.info("receive message from {}", msg.getSender());
    }
}
