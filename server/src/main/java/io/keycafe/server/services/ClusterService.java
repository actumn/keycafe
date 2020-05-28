package io.keycafe.server.services;

import io.keycafe.server.Server;
import io.keycafe.server.cluster.handler.ClusterMessageHandler;
import io.keycafe.server.network.decoder.ByteToClusterMsgDecoder;
import io.keycafe.server.network.encoder.ClusterMsgEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class ClusterService implements Service {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final Server server;
    private final int port;

    public ClusterService(Server server, int port) {
        this.server = server;
        this.port = port;
    }

    @Override
    public void run() throws Exception {
        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new ClusterMsgEncoder());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
                        pipeline.addLast(new ByteToClusterMsgDecoder());
                        pipeline.addLast(new ClusterChannelHandler());
                        pipeline.addLast(new ClusterMessageHandler(server));
                    }
                });

        ChannelFuture f = bootstrap.bind(new InetSocketAddress("localhost", port));
        f.sync().channel().closeFuture().sync();
    }

    @Override
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
