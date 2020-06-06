package io.keycafe.server.services;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SlotChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(SlotChannelHandler.class);
    public SlotChannelHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
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
