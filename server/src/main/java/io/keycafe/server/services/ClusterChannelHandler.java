package io.keycafe.server.services;

import io.keycafe.server.cluster.handler.ClusterMessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClusterChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(ClusterChannelHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.info("connection Bus - {}", ctx.channel().localAddress());
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(new ClusterMessageHandler());
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info(cause);
        ctx.close();
    }
}
