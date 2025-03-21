package Client.netty.handler;

import common.Message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

// Netty 中用于处理服务器端响应的处理器。
// 主要功能是接收来自服务器的 RpcResponse 对象，并在处理过程中管理连接的生命周期。
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 接收到response, 给channel设计别名，让sendRequest里读取response
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RPCResponse");
        //将服务器返回的 RpcResponse存储到当前的 Channel上，确保在之后的处理流程中能够获取到这个响应
        ctx.channel().attr(key).set(response);
        ctx.channel().close();
    }

    //用于捕获运行过程中出现的异常，进行处理并释放资源
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
