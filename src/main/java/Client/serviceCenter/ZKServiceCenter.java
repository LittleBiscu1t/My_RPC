package Client.serviceCenter;

import Client.cache.serviceCache;
import Client.serviceCenter.ZKWatcher.watchZK;
import Client.serviceCenter.balance.impl.ConsistencyHashBalance;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZKServiceCenter implements ServiceCenter{
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    // zookeeper根路径节点
    private static final String ROOT_PATH = "MyRPC";
    private static final String RETRY = "CanRetry";
    // 服务缓存
    private serviceCache cache;
    // 记录已监听服务
    private final Set<String> watchedServices = new HashSet<>();

    //负责zookeeper客户端的初始化，并与zookeeper服务端进行连接
    public ZKServiceCenter() throws InterruptedException {
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(policy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
        //初始化本地缓存
        cache=new serviceCache();
//        //加入zookeeper事件监听器
//        watchZK watcher=new watchZK(client,cache);
//        //监听启动
//        watcher.watchToUpdate(ROOT_PATH);
    }
    //根据服务名（接口名）返回地址
    @Override
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 先从缓存查
            List<String> serviceList = cache.getServcieFromCache(serviceName);

            // 如果缓存没有，说明是第一次请求该服务
            if (serviceList == null || serviceList.isEmpty()) {
                // 拉取 zk 中的子节点（地址列表）
                serviceList = client.getChildren().forPath("/" + serviceName);

                // 加入缓存
                for (String address : serviceList) {
                    cache.addServiceToCache(serviceName, address);
                }

                // 懒监听（首次使用该服务时注册监听）
                synchronized (watchedServices) {
                    if (!watchedServices.contains(serviceName)) {
                        watchZK watcher = new watchZK(client, cache);
                        watcher.watchToUpdate("/" + serviceName);
                        watchedServices.add(serviceName);
                        System.out.println("已监听服务: " + serviceName);
                    }
                }
            }

            if (serviceList.isEmpty()) {
                System.err.println("未发现服务实例: " + serviceName);
                return null;
            }

            // 使用一致性哈希算法进行负载均衡
            String address = new ConsistencyHashBalance().balance(serviceList);
            return parseAddress(address);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkRetry(String serviceName) {
        boolean canRetry =false;
        try {
            List<String> serviceList = client.getChildren().forPath("/" + RETRY);
            for(String s:serviceList){
                if(s.equals(serviceName)){
                    System.out.println("服务"+serviceName+"在白名单上，可进行重试");
                    canRetry=true;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return canRetry;
    }

    // 地址 -> XXX.XXX.XXX.XXX:port 字符串
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() +
                ":" +
                serverAddress.getPort();
    }
    // 字符串解析为地址
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}