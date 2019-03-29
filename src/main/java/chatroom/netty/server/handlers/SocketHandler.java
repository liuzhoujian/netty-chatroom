package chatroom.netty.server.handlers;

import chatroom.netty.protocol.IMMessage;
import chatroom.netty.server.processor.IMProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 自定义协议处理器
 */
public class SocketHandler extends SimpleChannelInboundHandler<IMMessage> {

    /*处理组件*/
    private IMProcessor processor = new IMProcessor();

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, IMMessage msg) throws Exception {
        processor.process(ctx.channel(), msg);

        /*System.out.println(msg);
        ctx.writeAndFlush(msg);*/
    }
}
