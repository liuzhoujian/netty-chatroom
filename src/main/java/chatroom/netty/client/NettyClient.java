package chatroom.netty.client;

import chatroom.netty.client.handler.ClientHandler;
import chatroom.netty.protocol.IMDecoder;
import chatroom.netty.protocol.IMEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    private int port;
    private String host;

    private String nickName;
    private ClientHandler clientHandler;

    public NettyClient(String nickName) {
        this.nickName = nickName;
        clientHandler = new ClientHandler(this.nickName);
    }

    public void connect(String host, int port) {
        this.host = host;
        this.port = port;

        EventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture future = null;
        try {
            Bootstrap b = new Bootstrap();
            b.channel(NioSocketChannel.class);

            b.group(group).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel sc) throws Exception {
                    ChannelPipeline pipeline = sc.pipeline();
                    //自定义handler
                    pipeline.addLast(new IMDecoder());
                    pipeline.addLast(new IMEncoder());
                    pipeline.addLast(clientHandler);
                }
            });

            //连接服务端
            future = b.connect(host, port).sync();
            System.out.println("客户端启动成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if(future != null) {
                    future.channel().closeFuture().sync();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //优雅关闭
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyClient("NettyClient").connect("localhost", 8080);
    }
}
