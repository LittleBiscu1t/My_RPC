package common.serializer.myCode;

import common.Message.MessageType;
import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MyDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        //1.读取消息类型
        short messageType = in.readShort();
        // 现在还只支持request与response请求
        if (messageType != MessageType.REQUEST.getCode() &&
                messageType != MessageType.RESPONSE.getCode()) {
            System.out.println("暂不支持此种数据");
            return;
        }
        //2.读取序列化的方式&类型
        // 序列化的方式由消息发送方在消息中决定
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        if (serializer == null)
            throw new RuntimeException("不存在对应的序列化器");
        //3.读取序列化数组长度
        // 与version1中实现的自定义通信协议功能相同，都解决了TCP粘包问题
        int length = in.readInt();
        //4.读取序列化数组
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object deserialize = serializer.deserialize(bytes, messageType);
        out.add(deserialize);
    }
}