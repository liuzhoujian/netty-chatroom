package chatroom.netty.server.processor;

import chatroom.netty.protocol.IMDecoder;
import chatroom.netty.protocol.IMEncoder;
import chatroom.netty.protocol.IMMessage;
import chatroom.netty.protocol.IMP;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * 处理用户登陆、登出、聊天...等组件
 */
public class IMProcessor {

    //保存在线用户
    private final static ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private IMDecoder decoder = new IMDecoder();
    private IMEncoder encoder = new IMEncoder();

    private final AttributeKey<String> NICK_NAME = AttributeKey.valueOf("nickName");
    private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    private final AttributeKey<String> ATTRS = AttributeKey.valueOf("attrs");

    public void logout(Channel channel) {
        onlineUsers.remove(channel);
    }

    //处理逻辑
    public void process(Channel client, String msg) {
        System.out.println(msg);

        IMMessage request = decoder.decode(msg);
        if(request == null) return;

        String nickName = request.getSender();

        //处理登陆逻辑
        if(IMP.LOGIN.getName().equals(request.getCmd())) {

            client.attr(NICK_NAME).getAndSet(request.getSender());//将昵称保存在对应的channel中

            //将新用户保存在集合中
            onlineUsers.add(client);

            //通知其他用户
            for(Channel channel : onlineUsers) {
                if(channel != client) {
                    //提示谁登陆了
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), nickName + "进入聊天室！");
                } else {
                    //提示自己已连接成功
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), "您已与服务器建立连接！");
                }

                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }

        } else if(IMP.LOGOUT.getName().equals(request.getCmd())) {
            //从集合中移除用户
            onlineUsers.remove(client);

            for(Channel channel : onlineUsers) {
                if(channel != client) {
                    //提示谁退出了
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), nickName + "退出聊天室！");
                }
                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }

        } else if(IMP.CHAT.getName().equals(request.getCmd())) {
            for(Channel channel : onlineUsers) {
                if(channel != client) {
                    request.setSender(client.attr(NICK_NAME).get());
                } else {
                    request.setSender("you");
                }

                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }

        } else if(IMP.FLOWER.getName().equals(request.getCmd())) {

        }
    }

    /*获取系统时间*/
    private long sysTime() {
        return System.currentTimeMillis();
    }
}
