package Server.serviceRegister;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    //注册：保存服务地址
    void register(String serviceName, InetSocketAddress serviceAddress, boolean canRetry);
}
