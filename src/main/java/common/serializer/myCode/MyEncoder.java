package common.serializer.myCode;

import common.Message.MessageType;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 *  依次按照自定义的消息格式写入，传入的数据为request或者response
 *  需要持有一个serialize器，负责将传入的对象序列化成字节数组
 */
@AllArgsConstructor
public class MyEncoder extends MessageToByteEncoder {
    //具体使用哪个 Serializer 实现，是通过构造函数传进来的，取决于在创建 MyEncoder 实例时传了哪个实现类进去
    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        System.out.println(msg.getClass());
        //1.写入消息类型
        if(msg instanceof RpcResponse){
            out.writeShort(MessageType.RESPONSE.getCode());
        }else if(msg instanceof RpcRequest){
            out.writeShort(MessageType.REQUEST.getCode());
        }
        //写入序列化方式
        out.writeShort(serializer.getType());
        //得到序列化数组
        byte[] bytes = serializer.serialize(msg);
        //3.写入长度
        // 这里用于记录长度的是一个int，也就是4个字节，与version1中实现的自定义通信协议功能相同，都解决了TCP粘包问题
        out.writeInt(bytes.length);
        //4.写入序列化数组
        // 将解码后的对象添加到out中，供下一个handler处理
        out.writeBytes(bytes);
    }
}
