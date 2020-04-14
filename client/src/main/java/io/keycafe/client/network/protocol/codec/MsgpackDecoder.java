// http://www.programmersought.com/article/35931792015/
package io.keycafe.client.network.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        final byte[] bytes;
        final int length = msg.readableBytes();
        bytes = new byte[length];

        msg.getBytes(msg.readerIndex(), bytes, 0, length);

        MessagePack msgpack = new MessagePack();
        out.add(msgpack.read(bytes));
    }
}
