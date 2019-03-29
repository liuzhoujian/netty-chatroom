package chatroom.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.List;

/**
 * 将字符串解析为自定义通信协议IMMessage对象
 */
public class IMDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            final int length = in.readableBytes();
            final byte[] array = new byte[length];

            //变为一个字符串
            String content = new String(array, in.readableBytes(), length);
            System.out.println("IMDecoder:" + content);
            if(!(null == content || "".equals(content.trim()))) {
                if(!IMP.isIMP(content)) {
                    ctx.channel().pipeline().remove(this);
                    return;
                }
            }

            //反序列化的过程
            in.getBytes(in.readableBytes(), array, 0, length);
            //首先把字节转为messagePack,之后再把这个msg转为IMMessage
            out.add(new MessagePack().read(array, IMMessage.class));
            in.clear();
        } catch (IOException e) {
            ctx.pipeline().remove(this); //只有为自定义socket协议时，这个解码器才生效
            return;
        }
    }


    public IMMessage decode(String msg) {
        //客户端共三种消息：LOGIN\CHAT\FLOWER
        if(msg == null || "".equals(msg)) {
            return null;
        }

        if(msg.startsWith("[" + IMP.LOGIN.getName() + "]")) { //[LOGIN][time][nickname]
            msg = msg.substring(1, msg.length()-1);
            String[] msgArr = msg.split("]\\[");
            return new IMMessage(IMP.LOGIN.getName(), Long.parseLong(msgArr[1]), msgArr[2]);
        }

        else if(msg.startsWith("[" + IMP.LOGOUT.getName() + "]")) {//[LOGOUT][time][nickname]
            msg = msg.substring(1, msg.length()-1);
            String[] msgArr = msg.split("]\\[");
            return new IMMessage(IMP.LOGOUT.getName(), Long.parseLong(msgArr[1]), msgArr[2]);
        }
        
        else if(msg.startsWith("[" + IMP.CHAT.getName() + "]")) { //[CHAT][time][nickname][receiver]-content
            String[] headerAndContentArr = msg.split("-");
            String header = headerAndContentArr[0];
            String content = headerAndContentArr[1];
            header = header.substring(1, header.length()-1);
            String[] msgArr = header.split("]\\[");
            return new IMMessage(IMP.CHAT.getName(), Long.parseLong(msgArr[1]), msgArr[2], content);
        } 
        
        else if(msg.startsWith("[" + IMP.FLOWER.getName() + "]")) { //[FLOWER][time][nickname]
            msg = msg.substring(1, msg.length()-1);
            String[] msgArr = msg.split("]\\[");
            return new IMMessage(IMP.FLOWER.getName(), Long.parseLong(msgArr[1]), msgArr[2]);
        }
        return null;
    }


}
