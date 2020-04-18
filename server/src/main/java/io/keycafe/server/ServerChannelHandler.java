package io.keycafe.server;

import io.keycafe.common.Protocol;
import io.keycafe.server.network.CommandMessage;
import io.keycafe.server.network.ReplyMessage;
import io.keycafe.server.network.command.CommandDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter {
    private final CommandDispatcher commandDispatcher;

    public ServerChannelHandler() {
        super();

        commandDispatcher = new CommandDispatcher();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CommandMessage command = (CommandMessage) msg;
        byte[][] argv = command.getArgv();

        System.out.println(command.getArgc());
        System.out.println(argv[0][0]);
        for (int i = 1; i < command.getArgc(); i++)
            System.out.println(new String(argv[i], Protocol.KEYCAFE_CHARSET));


        ReplyMessage reply = commandDispatcher.dispatch(command);
        ctx.writeAndFlush(reply);
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
