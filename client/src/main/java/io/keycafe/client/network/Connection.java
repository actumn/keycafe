package io.keycafe.client.network;

import io.keycafe.client.network.protocol.Protocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/*
prototype 으로 빠르게 구현해 보기 위해 netty 를 사용.
TODO:: blocking-IO 를 구현할 거라서 socket 을 쓰는 것으로 변경
Jedis 참고 (https://github.com/xetorthio/jedis/blob/master/src/main/java/redis/clients/jedis/Connection.java)
 */
public class Connection {
    Channel channel;

    public void connect() throws InterruptedException {
        InetSocketAddress remote = new InetSocketAddress("127.0.0.1", Protocol.DEFAULT_PORT);

        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                    }
                });

        channel = bootstrap.connect(remote).sync().channel();
    }

    public void send() {
        // 뭘 보내지 ..?
    }
}
