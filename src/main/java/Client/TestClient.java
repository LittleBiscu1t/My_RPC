package Client;

import Client.proxy.ClientProxy;
import common.service.UserService;
import common.pojo.User;

public class TestClient {
    public static void main(String[] args) throws InterruptedException {
//        ClientProxy clientProxy=new ClientProxy("127.0.0.1",9999, 0);
        ClientProxy clientProxy=new ClientProxy();
        UserService proxy=clientProxy.getProxy(UserService.class);

        for(int i = 0; i < 120; i++) {
            Integer i1 = i;
            if (i%30==0) {
                Thread.sleep(10000);
            }
            new Thread(()->{
                try{
                    User user = proxy.getUserByUserId(i1);

                    System.out.println("从服务端得到的user="+user.toString());

                    Integer id = proxy.insertUserId(User.builder().id(i1).userName("User" + i1.toString()).sex(true).build());
                    System.out.println("向服务端插入user的id"+id);
                } catch (NullPointerException e){
                    System.out.println("user为空");
                    e.printStackTrace();
                }
            }).start();
        }

//        User user = proxy.getUserByUserId(1);
//        System.out.println("从服务端得到的user="+user.toString());
//
//        User u=User.builder().id(100).userName("lwx").sex(true).build();
//        Integer id = proxy.insertUserId(u);
//        System.out.println("向服务端插入user的id"+id);
    }
}