package io.keycafe.server.network.encoder;

import io.keycafe.common.Protocol;
import io.keycafe.server.command.ReplyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ReplyEncoder extends MessageToByteEncoder<ReplyMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ReplyMessage msg, ByteBuf out) throws Exception {
        String message = msg.message();
        out.writeByte(message.length());
        out.writeBytes(message.getBytes(Protocol.KEYCAFE_CHARSET));
    }
}
