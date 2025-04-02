package common.serializer.mySerializer.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import common.Message.RpcRequest;
import common.Message.RpcResponse;
import common.serializer.mySerializer.Serializer;

/**
 * @author wxx
 * @version 1.0
 * @create 2024/6/2 22:31
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
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
                    Object param = request.getParams()[i];
                    if (param == null) {
                        objects[i] = null;
                    } else if (!paramsType.isAssignableFrom(param.getClass())) {
                        objects[i] = JSONObject.toJavaObject((JSONObject) param, paramsType);
                    } else {
                        objects[i] = param;
                    }
                }
                request.setParams(objects);
                obj = request;
                break;
            case 1:
                RpcResponse response = JSON.parseObject(bytes, RpcResponse.class);
                Object data = response.getData();
                Class<?> dataType = response.getDataType();
                if (data != null && dataType != null && !dataType.isAssignableFrom(data.getClass())) {
                    response.setData(JSONObject.toJavaObject((JSONObject) data, dataType));
                }
                obj = response;
                break;
            default:
                System.out.println("暂时不支持此种消息");
                throw new RuntimeException();
        }
        return obj;
    }

    //1 代表json序列化方式
    @Override
    public int getType() {
        return 1;
    }
}
