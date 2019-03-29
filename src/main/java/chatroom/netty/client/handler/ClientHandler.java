package chatroom.netty.client.handler;

import chatroom.netty.protocol.IMMessage;
import chatroom.netty.protocol.IMP;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Scanner;

public class ClientHandler extends SimpleChannelInboundHandler<IMMessage> {
    private String nickName;
    public ClientHandler(String nickName) {
        this.nickName = nickName;
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, IMMessage message) throws Exception {
        System.out.println("收到服务端反馈：" + message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //向服务端发送登录消息
        IMMessage message = new IMMessage();
        message.setCmd(IMP.LOGIN.getName());
        message.setTime(systTime());
        message.setSender(this.nickName);
        System.out.println("向服务端发送登录消息");
        ctx.channel().writeAndFlush(message);

        //开启会话
        session(ctx);
    }

    private void session(ChannelHandlerContext ctx) {
        String nickName = this.nickName;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner in = new Scanner(System.in);
                IMMessage imMessage = new IMMessage();
                imMessage.setCmd(IMP.CHAT.getName());
                imMessage.setTime(systTime());
                imMessage.setSender(nickName);

                while (true) {
                    System.out.println(nickName + "-请输入消息：");
                    String msg = in.nextLine();
                    imMessage.setContent(msg);
                    ctx.channel().writeAndFlush(imMessage);
                }
            }
        }).start();
    }

    private long systTime() {
        return System.currentTimeMillis();
    }
}
