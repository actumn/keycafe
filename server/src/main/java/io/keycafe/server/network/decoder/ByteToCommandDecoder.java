package io.keycafe.server.network.decoder;

import io.keycafe.server.command.CommandMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ByteToCommandDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte argc = msg.readByte();
        byte[][] argv = new byte[argc][];

        for (int i = 0; i < argc; i++) {
            byte argLen = msg.readByte();

            byte[] arg = new byte[argLen];
            msg.readBytes(arg, 0, argLen);

            argv[i] = arg;
        }

        out.add(new CommandMessage(argc, argv));
    }
}

