package io.keycafe.server.cluster.handler;

import io.keycafe.server.cluster.ClusterMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClusterMessageHandler extends SimpleChannelInboundHandler<ClusterMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClusterMessage msg) throws Exception {

    }
}
