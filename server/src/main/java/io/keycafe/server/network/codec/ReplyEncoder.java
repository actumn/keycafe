package io.keycafe.server.network.codec;

import io.keycafe.server.network.ReplyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ReplyEncoder extends MessageToByteEncoder<ReplyMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ReplyMessage msg, ByteBuf out) throws Exception {
        out.writeBytes("ok".getBytes());
    }
}
