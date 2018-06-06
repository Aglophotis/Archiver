package ru.mirea.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    private int port;
	
	public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
		else port = 60101;
        new Server(port).run();
    }
	
    public Server(int port) {
        this.port = port;
    }
	
    public void run() throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) /*throws Exception */{
                     ch.pipeline().addLast(new ServerHandler());
                     ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(64000));
                     System.out.println("Starting server on: " + ch.localAddress().getHostName());
                 }
             })
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true);
    
            ChannelFuture f = b.bind(port).sync();
    
            f.channel().closeFuture().sync();
        }
		finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}