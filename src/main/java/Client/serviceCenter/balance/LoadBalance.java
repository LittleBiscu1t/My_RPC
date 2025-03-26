package Client.serviceCenter.balance;

import java.util.List;

public interface LoadBalance {
    //负责具体的算法实现，返回分配的地址
    String balance(List<String> addressList);
    //添加节点
    void addNode(String node);
    //删除节点
    void delNode(String node);
}
