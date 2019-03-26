package chatroom.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 将IMMessage转为String
 */
public class IMEncoder extends MessageToByteEncoder<IMMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IMMessage msg, ByteBuf byteBuf) throws Exception {

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
