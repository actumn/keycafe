package io.keycafe.server.cluster;

import io.keycafe.server.cluster.handler.ClusterMessageHandler;
import io.keycafe.server.network.decoder.ByteToClusterMsgDecoder;
import io.keycafe.server.network.encoder.ClusterMsgEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClusterConnector {
    public ClusterLink connect(String hostAddress, int port) {
        EventLoopGroup loopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(loopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new ClusterMsgEncoder());
                        pipeline.addLast(new ByteToClusterMsgDecoder());
                        pipeline.addLast(new ClusterMessageHandler());
                    }
                });

        SocketChannel ch = (SocketChannel) bootstrap.connect(hostAddress, port).channel();
        return new ClusterLink(ch);
    }
}
