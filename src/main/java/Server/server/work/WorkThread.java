package Server.server.work;


import lombok.AllArgsConstructor;
import Server.provider.ServiceProvider;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;


@AllArgsConstructor

/**
 * Runnable 接口的作用
 * Runnable 是 Java 中用于定义 线程任务 的核心接口，作用如下：
 *
 * 封装任务逻辑：Runnable 允许将任务逻辑封装在 run() 方法中，使任务可以在多个线程中执行。
 * 支持并发执行：当 Runnable 实现类被传递给 Thread 或 ExecutorService 时，可以并发执行多个实例，提高性能。
 * 避免直接继承 Thread：Java 不支持多继承，而 Runnable 作为接口，使类仍然可以继承其他类，同时定义自己的业务逻辑。
 */
public class WorkThread implements Runnable{
    private Socket socket;
    private ServiceProvider serviceProvide;
    @Override
    public void run() {
        try {
            ObjectOutputStream oos=new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
            //读取客户端传过来的request
            RpcRequest rpcRequest = (RpcRequest) ois.readObject();
            //反射调用服务方法获取返回值
            RpcResponse rpcResponse=getResponse(rpcRequest);
            //向客户端写入response
            oos.writeObject(rpcResponse);
            oos.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private RpcResponse getResponse(RpcRequest rpcRequest){
        //得到服务名
        String interfaceName=rpcRequest.getInterfaceName();
        //得到服务端相应服务实现类
        Object service = serviceProvide.getService(interfaceName);
        //反射调用方法
        Method method=null;
        try {
            method= service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke=method.invoke(service,rpcRequest.getParams());
            return RpcResponse.sussess(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            System.out.println("方法执行错误");
            return RpcResponse.fail();
        }
    }
}
