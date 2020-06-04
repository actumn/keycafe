package io.keycafe.server.services;

import io.keycafe.common.Protocol.Command;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.command.handler.*;
import io.keycafe.server.slot.LocalSlot;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.util.HashMap;
import java.util.Map;

public class SlotChannelHandler extends ChannelInboundHandlerAdapter {
    private final LocalSlot slot;
    private final Map<String, ClusterNode> nodeMap;

    public SlotChannelHandler(LocalSlot slot, Map<String, ClusterNode> nodeMap){
        this.slot = slot;
        this.nodeMap = nodeMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        final ChannelPipeline pipeline = ctx.pipeline();
        Map<Command, CommandRunnable> commandMap = new HashMap<>();
        commandMap.put(Command.GET, new GetCommand(slot.db));
        commandMap.put(Command.SET, new SetCommand(slot.db, slot.expire));
        commandMap.put(Command.DELETE, new DeleteCommand(slot.db));
        commandMap.put(Command.CLUSTER, new ClusterCommand(nodeMap));

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
