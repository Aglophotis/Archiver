package ru.mirea.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Client {

    public static void main(String[] args) throws Exception {

        String host = "localhost";
        int port = Integer.parseInt("60101");
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        System.out.println("Initializing client on a port: " + port + " and host: " + host);

        try {

            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(64000));
                    ch.pipeline().addLast(new ClientHandler());
                    ch.pipeline().addLast(new ObjectEncoder());
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        }
		finally {
            workerGroup.shutdownGracefully();
        }
    }
}