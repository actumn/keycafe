package io.keycafe.server.network.encoder;

import io.keycafe.server.command.ReplyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ClusterMsgEncoder extends MessageToByteEncoder<ReplyMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ReplyMessage msg, ByteBuf out) throws Exception {

    }
}
