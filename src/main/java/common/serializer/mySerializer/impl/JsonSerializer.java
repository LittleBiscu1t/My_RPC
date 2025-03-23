package common.serializer.mySerializer.impl;

import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serializer.mySerializer.Serializer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

public class JsonSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        //使用fastjson提供的方法，将任意对象转换为JSON格式的字符数组
        byte[] bytes = JSONObject.toJSONBytes(obj);
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        // 传输的消息分为request与response
        switch (messageType){
            case 0:
                RpcRequest request = JSON.parseObject(bytes, RpcRequest.class);
                Object[] objects = new Object[request.getParams().length];
                // 对转换后的request中的params属性逐个进行类型判断
                // 把json字串转化成对应的对象， fastjson可以读出基本数据类型，不用转化
                // JSON 反序列化后，params[i] 可能是个 JSONObject(如果不是基本数据类型)，不是实际类型（如 User 或 int 等）
                for(int i = 0; i < objects.length; i++){
                    Class<?> paramsType = request.getParamsType()[i];
                    //判断每个对象类型是否和paramsTypes中的一致
                    /*
                    例如：
                    paramsType = com.example.User.class
                    param = JSONObject 类型，表示 User 对象
                    param.getClass() = JSONObject.class
                     */
                    if (!paramsType.isAssignableFrom(request.getParams()[i].getClass())){
                        //如果不一致，就行进行类型转换
                        objects[i] = JSONObject.toJavaObject((JSONObject) request.getParams()[i],request.getParamsType()[i]);
                    }else{
                        //如果一致就直接赋给objects[i]
                        objects[i] = request.getParams()[i];
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1:
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);
                Class<?> dataType = response.getDataType();
                //判断转化后的response对象中的data的类型是否正确
                if(! dataType.isAssignableFrom(response.getData().getClass())){
                    response.setData(JSONObject.toJavaObject((JSONObject) response.getData(),dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
}
