package Client.proxy;


import Client.circuitBreaker.CircuitBreaker;
import Client.circuitBreaker.CircuitBreakerProvider;
import Client.retry.guavaRetry;
import Client.rpcClient.RpcClient;
import Client.rpcClient.impl.NettyRpcClient;
import Client.serviceCenter.ServiceCenter;
import Client.serviceCenter.ZKServiceCenter;
import lombok.AllArgsConstructor;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@AllArgsConstructor
public class ClientProxy implements InvocationHandler {
    //传入参数service接口的class对象，反射封装成一个request
    private RpcClient rpcClient;
    private ServiceCenter serviceCenter;
    private CircuitBreakerProvider circuitBreakerProvider;

    public ClientProxy() throws InterruptedException {
        serviceCenter=new ZKServiceCenter();
        rpcClient = new NettyRpcClient(serviceCenter);
        circuitBreakerProvider = new CircuitBreakerProvider();
    }

    //jdk动态代理，每一次代理对象调用方法，都会经过此方法增强（反射获取request对象，socket发送到服务端）
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //构建request
        RpcRequest request=RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes()).build();
        //获取熔断器
        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        //判断熔断器是否允许请求经过
        if (!circuitBreaker.allowRequest()){
            //这里可以针对熔断做特殊处理，返回特殊值
            System.out.println("服务被熔断，对应的服务是：" + method.getName());
            return null;
        }
        //IOClient.sendRequest 和服务端进行数据传输
        RpcResponse response;
        //针对白名单上的服务，可以重试请求
        if(serviceCenter.checkRetry(request.getInterfaceName())){
            response = new guavaRetry().sendServiceWithRetry(request, rpcClient);
        }else{
            //非白名单服务，只发送一次请求
            response = rpcClient.sendRequest(request);
        }

        //记录response的状态，上报给熔断器
        if (response.getCode() ==200){
            circuitBreaker.recordSuccess();
        }
        if (response.getCode()==500){
            circuitBreaker.recordFailure();
        }

        return response.getData();
    }
     public <T>T getProxy(Class<T> clazz){
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T)o;
    }
}
