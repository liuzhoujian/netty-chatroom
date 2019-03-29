package chatroom.netty.protocol;

import io.netty.handler.codec.marshalling.*;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * 序列化和反序列化工具
 */
public class MarshallingCodeFactory {

    //首先通过Marshalling序列化工厂类，参数serial标识创建的是Java序列化工厂对象
    private static final MarshallerFactory marshallFactory =
            Marshalling.getProvidedMarshallerFactory("serial");

    //创建MarshallingConfiguration对象，版本号为5
    private static final MarshallingConfiguration configuration =
            new MarshallingConfiguration();

    static {
        configuration.setVersion(5);
    }

    /**
     * 解码器
     * @return MarshallingDecoder
     */
    public static MarshallingDecoder buildMarshallingDecoder() {
        UnmarshallerProvider unmarshallerProvider = new DefaultUnmarshallerProvider(marshallFactory, configuration);
        //第二个参数：消息序列化的最大长度
        return new MarshallingDecoder(unmarshallerProvider, 1024 * 1024 * 1);
    }

    /**
     * 编码器
     * @return
     */
    public static MarshallingEncoder buildMarshallingEncoder() {
        MarshallerProvider marshallerProvider = new DefaultMarshallerProvider(marshallFactory, configuration);
        //marshallingEncoder将POJO对象序列化为二进制数组
        return new MarshallingEncoder(marshallerProvider);
    }

}
