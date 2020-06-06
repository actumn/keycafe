package io.keycafe.server.command.handler;

import io.keycafe.common.ClusterCRC16;
import io.keycafe.common.Protocol;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.cluster.ClusterState;
import io.keycafe.server.command.CommandMessage;
import io.keycafe.server.command.reply.ErrorMessage;
import io.keycafe.server.command.reply.ReplyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class CommandHandler extends SimpleChannelInboundHandler<CommandMessage> {
    private final Map<Protocol.Command, CommandRunnable> commandMap;
    private final ClusterState cluster;
    private final ClusterNode myself;

    public CommandHandler(Map<Protocol.Command, CommandRunnable> commandMap, ClusterState cluster, ClusterNode myself) {
        this.commandMap = commandMap;
        this.cluster = cluster;
        this.myself = myself;
    }

    private Protocol.Command lookup(byte[] command) {
        return Protocol.Command.values()[(int) command[0]];
    }
    private CommandRunnable lookupCommand(Protocol.Command command) {
        return commandMap.get(command);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommandMessage msg) throws Exception {
        byte[][] argv = msg.getArgv();
        Protocol.Command commandCode = lookup(argv[0]);
        CommandRunnable command = lookupCommand(commandCode);

        // redirect if slot not matched here
        int keyIndex = command.keyIndex();
        if (keyIndex > 0) {
            String key = new String(argv[keyIndex], Protocol.KEYCAFE_CHARSET);
            int hashSlot = ClusterCRC16.getSlot(key);
            ClusterNode node = cluster.getNodeBySlot(hashSlot);
            if (node != myself) {
                ctx.writeAndFlush(new ErrorMessage(
                        String.format("MOVED %d %s:%d", hashSlot, node.getHostAddress(), node.getPort())));
                return;
            }
        }

        // call
        ReplyMessage reply = command.run(msg.getArgc(), msg.getArgv());
        ctx.writeAndFlush(reply);
    }
}
