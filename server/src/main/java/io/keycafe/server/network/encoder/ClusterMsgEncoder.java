package io.keycafe.server.network.encoder;

import io.keycafe.server.cluster.ClusterMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ClusterMsgEncoder extends MessageToByteEncoder<ClusterMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ClusterMessage msg, ByteBuf out) throws Exception {
        out.writeBytes("KCmb".getBytes()); // signature
        out.writeByte(msg.getType().ordinal());
        out.writeBytes(msg.getMyslots());
        out.writeBytes(msg.getSender().getBytes());
        out.writeByte('\n');
    }
}
