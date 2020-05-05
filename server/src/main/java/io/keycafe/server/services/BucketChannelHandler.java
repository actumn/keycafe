package io.keycafe.server.services;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.command.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.HashMap;

public class BucketChannelHandler extends ChannelInboundHandlerAdapter {

    public BucketChannelHandler() {
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(new CommandHandler(new HashMap<Protocol.Command, CommandRunnable>() {
            {
                put(Protocol.Command.GET, new GetCommand());
                put(Protocol.Command.SET, new SetCommand());
                put(Protocol.Command.DELETE, new DeleteCommand());
            }
        }));
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
