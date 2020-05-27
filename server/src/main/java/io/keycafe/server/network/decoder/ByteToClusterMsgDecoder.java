package io.keycafe.server.network.decoder;

import io.keycafe.server.cluster.ClusterMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ByteToClusterMsgDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        byte[] signature = new byte[4];
        msg.readBytes(signature);
        if (!"KCmb".equals(new String(signature))) {
            return;
        }

        byte type = msg.readByte();
        byte[] slots = new byte[2048];
        msg.readBytes(slots);

        byte[] sender = new byte[40];
        msg.readBytes(sender);

        out.add(new ClusterMessage(ClusterMessage.ClusterMessageType.values()[(int) type], slots, new String(sender)));
    }
}
