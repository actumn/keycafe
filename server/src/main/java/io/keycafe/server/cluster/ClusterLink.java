package io.keycafe.server.cluster;

import io.keycafe.server.cluster.handler.ClusterMessageHandler;
import io.keycafe.server.network.decoder.ByteToClusterMsgDecoder;
import io.keycafe.server.network.encoder.ClusterMsgEncoder;
import io.keycafe.server.services.ClusterChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;

public class ClusterLink {
    private final ClusterNode clusterNode;
    private SocketChannel channel;

    public ClusterLink(ClusterNode clusterNode) {
        this.clusterNode = clusterNode;
    }

    public void connect(String hostAddress, int port) {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(loopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new ClusterMsgEncoder());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
                        pipeline.addLast(new ByteToClusterMsgDecoder());
                        pipeline.addLast(new ClusterChannelHandler());
                    }
                });

        channel = (SocketChannel) bootstrap.connect(hostAddress, port).channel();
    }

    public void sendPing(ClusterNode myself) {
        channel.writeAndFlush(new ClusterMessage(
                ClusterMessage.ClusterMessageType.PING,
                myself.getMyslots(),
                myself.getNodeId()));
    }
}
