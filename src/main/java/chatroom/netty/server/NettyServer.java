package chatroom.netty.server;

import chatroom.netty.protocol.IMDecoder;
import chatroom.netty.protocol.IMEncoder;
import chatroom.netty.server.handlers.MyHttpHandler;
import chatroom.netty.server.handlers.MyWebSocketHandler;
import chatroom.netty.server.handlers.SocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyServer {

    private int port = 8080;

    public void start(int port) {
        this.port = port;

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            /*netty启动引擎*/
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class)
                    //主从模式
                    .group(boss, worker)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel sc) throws Exception {
                            //-----------------支持HTTP---------------------------------
                            //http请求编码与解码
                            sc.pipeline().addLast(new HttpServerCodec());
                            //目的是将多个消息转换为单一的request或者response对象
                            sc.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
                            //目的是支持异步大文件传输
                            sc.pipeline().addLast(new ChunkedWriteHandler());
                            //自定义的http处理器(遇到ws的不进行处理，向下面的handler传递)
                            sc.pipeline().addLast(new MyHttpHandler("/ws"));

                            //-----------------支持WebSocket---------------------------------
                            sc.pipeline().addLast(new WebSocketServerProtocolHandler("/im"));
                            sc.pipeline().addLast(new MyWebSocketHandler());

                            //--------支持自定义协议处理（由于是自定义协议，netty内部没有编解码器，所以要自己实现）------
                            sc.pipeline().addLast(new IMEncoder());//自定义编码器
                            sc.pipeline().addLast(new IMDecoder());//自定义解码器
                            sc.pipeline().addLast(new SocketHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //绑定端口
            ChannelFuture future = serverBootstrap.bind("localhost", this.port).sync();
            System.out.println("服务器已启动：" + this.port);
            //等待关闭通道
            future.channel().closeFuture().sync();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyServer().start(8080);
    }
}
