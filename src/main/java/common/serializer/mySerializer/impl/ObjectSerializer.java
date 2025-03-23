package common.serializer.mySerializer.impl;

import common.serializer.mySerializer.Serializer;

import java.io.*;

public class ObjectSerializer implements Serializer {
    //利用Java io将对象序列化为字节数组
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        // 创建一个内存中的输出流，用于存储序列化后的字节数据
        // ByteArrayOutputStream是一个可变大小的字节数据缓冲区，数据都会写入这个缓冲区中
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            // oos是一个对象输出流，用于将 Java 对象序列化为字节流，并将其连接到bos上
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            //把对象写入输出流中
            oos.writeObject(obj);
            oos.flush();
            //强制将缓冲区的数据刷新到底层流bos中
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes, int messageType) {
        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }
}
