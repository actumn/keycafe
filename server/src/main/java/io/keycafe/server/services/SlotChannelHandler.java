package io.keycafe.server.services;

import io.keycafe.common.Protocol;
import io.keycafe.common.Protocol.Command;
import io.keycafe.server.command.*;
import io.keycafe.server.slot.LocalSlot;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.HashMap;
import java.util.Map;

public class SlotChannelHandler extends ChannelInboundHandlerAdapter {

    private Map<String, String> kvStore;
    private Map<String, Long> expireStore;

    public SlotChannelHandler(LocalSlot slot){
        this.kvStore = slot.kvStore;
        this.expireStore = slot.expireStore;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        final ChannelPipeline pipeline = ctx.pipeline();
        pipeline.addLast(new CommandHandler(new HashMap<Command, CommandRunnable>() {
            {
                put(Protocol.Command.GET, new GetCommand(kvStore));
                put(Protocol.Command.SET, new SetCommand(kvStore, expireStore));
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
