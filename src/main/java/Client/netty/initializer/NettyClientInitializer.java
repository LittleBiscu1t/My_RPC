package Client.netty.initializer;

import common.serializer.myCode.MyDecoder;
import common.serializer.myCode.MyEncoder;
import common.serializer.mySerializer.impl.JsonSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import Client.netty.handler.NettyRpcClientHandler;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //使用自定义的编解码器
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        pipeline.addLast(new NettyRpcClientHandler());

//        ChannelPipeline pipeline = ch.pipeline();
//        //下面向ChannelPipeline加入多个ChannelHandler
//        //消息格式 【长度】【消息体】，解决沾包问题
//        pipeline.addLast(
//                new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//        //计算当前待发送消息的长度，写入到前4个字节中
//        pipeline.addLast(new LengthFieldPrepender(4));
//        //编码器
//        //使用Java序列化方式，netty的自带的解码编码支持传输这种结构
//        pipeline.addLast(new ObjectEncoder());
//        //解码器
//        //使用了Netty中的ObjectDecoder，它用于将字节流解码为 Java 对象。
//        //在ObjectDecoder的构造函数中传入了一个ClassResolver 对象，用于解析类名并加载相应的类
//        pipeline.addLast(new ObjectDecoder(new ClassResolver() {
//            @Override
//            public Class<?> resolve(String className) throws ClassNotFoundException {
//                /*
//                Class.forName(className) 是 Java 反射的核心方法之一，它的作用是：
//                通过 类的全限定名（包名 + 类名） 在运行时动态加载类。
//                加载的类 不需要在编译时确定，而是 在运行时决定 具体是哪一个类。
//                 */
//                return Class.forName(className);
//            }
//        }));
//        //把NettyClientHandler加入pipeline
//        pipeline.addLast(new NettyRpcClientHandler());
    }
}
