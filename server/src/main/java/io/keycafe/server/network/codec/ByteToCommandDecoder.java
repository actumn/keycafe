package io.keycafe.server.network.codec;

import io.keycafe.common.Protocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ByteToCommandDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (msg.readableBytes() < 4) {
            return;
        }
        System.out.println(msg.readableBytes());

        byte len = msg.readByte();
        byte commandLen = msg.readByte();
        byte commandType = msg.readByte();
        Protocol.Command command = Protocol.Command.values()[commandType];
        System.out.println(command.toString());

        for (int i = 0; i < len - 1; i++) {
            byte argLen = msg.readByte();

            byte[] arg = new byte[argLen];
            msg.readBytes(arg, 0, argLen);

            System.out.println(new String(arg, Protocol.KEYCAFE_CHARSET));
        }

        out.add("ok");
    }
}

