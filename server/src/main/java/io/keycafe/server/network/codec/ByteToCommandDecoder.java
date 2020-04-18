package io.keycafe.server.network.codec;

import io.keycafe.server.network.CommandMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ByteToCommandDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte argc = msg.readByte();
        byte[][] argv = new byte[argc][];

        byte commandLen = msg.readByte();
        byte[] arg0 = new byte[commandLen];
        msg.readBytes(arg0, 0, commandLen);
        argv[0] = arg0;

        for (int i = 0; i < argc - 1; i++) {
            byte argLen = msg.readByte();

            byte[] arg = new byte[argLen];
            msg.readBytes(arg, 0, argLen);

            argv[i+1] = arg;
        }

        out.add(new CommandMessage(argc, argv));
    }
}

