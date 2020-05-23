package io.keycafe.server.services;

import io.keycafe.common.Protocol;
import io.keycafe.common.Protocol.Command;
import io.keycafe.server.command.handler.*;
import io.keycafe.server.slot.LocalSlot;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.HashMap;
import java.util.Map;

public class SlotChannelHandler extends ChannelInboundHandlerAdapter {
    private final LocalSlot slot;

    public SlotChannelHandler(LocalSlot slot){
        this.slot = slot;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        final ChannelPipeline pipeline = ctx.pipeline();
        Map<Command, CommandRunnable> commandMap = new HashMap<>();
        commandMap.put(Protocol.Command.GET, new GetCommand(slot.db));
        commandMap.put(Protocol.Command.SET, new SetCommand(slot.db, slot.expire));
        commandMap.put(Protocol.Command.DELETE, new DeleteCommand(slot.db));

        pipeline.addLast(new CommandHandler(commandMap));
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
