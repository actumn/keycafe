package io.keycafe.server.network.encoder;

import io.keycafe.common.Protocol;
import io.keycafe.server.command.reply.ReplyMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReplyEncoder extends MessageToByteEncoder<ReplyMessage> {
    private static final Logger logger = LogManager.getLogger(ReplyEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ReplyMessage msg, ByteBuf out) throws Exception {
        String message = msg.message();
        logger.info("reply message - {}", message);
        out.writeBytes(message.getBytes(Protocol.KEYCAFE_CHARSET));
    }
}
