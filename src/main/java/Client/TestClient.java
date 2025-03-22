package Client;

import Client.proxy.ClientProxy;
import common.service.UserService;
import common.pojo.User;

public class TestClient {
    public static void main(String[] args) {
//        ClientProxy clientProxy=new ClientProxy("127.0.0.1",9999, 0);
        ClientProxy clientProxy=new ClientProxy();
        UserService proxy=clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());

        User u=User.builder().id(100).userName("lwx").sex(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id"+id);
    }
}