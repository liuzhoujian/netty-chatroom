package chatroom.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

import java.io.IOException;

/**
 * 将IMMessage转为String
 */
public class IMEncoder extends MessageToByteEncoder<IMMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out) throws Exception {
        try {
            //利用messagePack将IMMessage对象转为字符串
            out.writeBytes(new MessagePack().write(msg));
        } catch (IOException e) {
            e.printStackTrace();
            ctx.pipeline().remove(this);
            return;
        }
    }

    public String encode(IMMessage msg) {
        if(null == msg) {
            return "";
        }

        String prefix = "[" + msg.getCmd() + "][" + msg.getTime() +"]";

        if(IMP.LOGIN.getName().equals(msg.getCmd()) ||
           IMP.CHAT.getName().equals(msg.getCmd()) ||
           IMP.FLOWER.getName().equals(msg.getCmd())) {

            prefix += ("[" + msg.getSender() + "]");

        } else if(IMP.SYSTEM.getName().equals(msg.getCmd())) {
            prefix += ("[" + msg.getOnline() + "]");
        }

        if( !(null == msg.getContent() || "".equals(msg.getContent()))) {
            prefix += ("-" + msg.getContent());
        }

        return prefix;
    }
}
