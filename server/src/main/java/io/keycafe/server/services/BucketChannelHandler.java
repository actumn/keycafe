package io.keycafe.server.services;

import io.keycafe.common.Protocol;
import io.keycafe.common.Protocol.Command;
import io.keycafe.server.command.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BucketChannelHandler extends ChannelInboundHandlerAdapter {

    public Map<String, String> kvStore = new ConcurrentHashMap<>();

    public BucketChannelHandler() {
        super();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(new CommandHandler(new HashMap<Command, CommandRunnable>() {
            {
                put(Protocol.Command.GET, new GetCommand(kvStore));
                put(Protocol.Command.SET, new SetCommand(kvStore));
                put(Protocol.Command.DELETE, new DeleteCommand(kvStore));
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
