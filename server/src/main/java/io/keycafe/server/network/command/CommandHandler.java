package io.keycafe.server.network.command;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.CommandMessage;
import io.keycafe.server.network.ReplyMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class CommandHandler extends SimpleChannelInboundHandler<CommandMessage> {
    private final Map<Protocol.Command, CommandRunnable> commandMap;

    public CommandHandler(Map<Protocol.Command, CommandRunnable> commandMap) {
        this.commandMap = commandMap;
    }

    private Protocol.Command lookup(byte[] command) {
        return Protocol.Command.values()[(int) command[0]];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CommandMessage msg) throws Exception {
        byte[][] argv = msg.getArgv();
        Protocol.Command command = lookup(argv[0]);

//        ReplyMessage reply = commandMap.get(command).run(msg.getArgc(), msg.getArgv());
        ctx.writeAndFlush(ReplyMessage.OkMessage);


        System.out.println("argc: " + msg.getArgc());
        System.out.println("argv[0]: " + argv[0][0]);
        for (int i = 1; i < msg.getArgc(); i++)
            System.out.println("argv["+i+"]: " + new String(argv[i], Protocol.KEYCAFE_CHARSET));
    }
}
