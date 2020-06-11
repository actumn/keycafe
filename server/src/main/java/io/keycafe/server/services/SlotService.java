package io.keycafe.server.services;

import io.keycafe.common.Protocol;
import io.keycafe.server.cluster.ClusterNode;
import io.keycafe.server.cluster.ClusterState;
import io.keycafe.server.command.handler.*;
import io.keycafe.server.network.decoder.ByteToCommandDecoder;
import io.keycafe.server.network.encoder.ReplyEncoder;
import io.keycafe.server.slot.LocalSlot;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class SlotService implements Service {
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final int port;
    private final LocalSlot lslot;
    private final ClusterState cluster;
    private final ClusterNode myself;

    public SlotService(LocalSlot lslot, ClusterState cluster, ClusterNode myself, int port) {
        this.lslot = lslot;
        this.port = port;
        this.cluster = cluster;
        this.myself = myself;
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

                        pipeline.addLast(new ByteToCommandDecoder());
                        pipeline.addLast(new ReplyEncoder());
                        pipeline.addLast(new SlotChannelHandler());

                        Map<Protocol.Command, CommandRunnable> commandMap = new HashMap<>();
                        commandMap.put(Protocol.Command.GET, new GetCommand(lslot.db));
                        commandMap.put(Protocol.Command.SET, new SetCommand(lslot.db, lslot.expire));
                        commandMap.put(Protocol.Command.DELETE, new DeleteCommand(lslot.db));
                        commandMap.put(Protocol.Command.CLUSTER, new ClusterCommand(cluster.getNodeMap()));
                        pipeline.addLast(new CommandHandler(commandMap, cluster, myself));
                    }
                });

        ChannelFuture f = bootstrap.bind(port);
        f.sync().channel().closeFuture().sync();
    }

    @Override
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
