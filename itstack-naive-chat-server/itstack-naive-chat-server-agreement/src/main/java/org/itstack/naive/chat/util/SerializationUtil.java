package org.itstack.naive.chat.util;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 虫洞栈：https://bugstack.cn
 * 公众号：bugstack虫洞栈  ｛关注获取学习源码｝
 */
public class SerializationUtil {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd();

    private SerializationUtil() {

    }

    /**
     * 序列化(对象 -> 字节数组)
     *
     * @param obj 对象
     * @return 字节数组
     */
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化(字节数组 -> 对象)
     *
     * @param data
     * @param cls
     * @param <T>
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, message, schema);

            /**
             * 在网上也看到人有如下写法，那么这两种写法有什么区别呢
             * T obj = schema.newMessage();
             * ProtostuffIOUtil.mergeFrom(data, obj, schema);
             * return message;
             *
             * 这里我们要主要创建message目的是什么，创建message对象的目的就是通过反射创建一个实例对象
             * 然后对这个对象进行赋值
             *
             * 所以schema.newMessage()和objenesis.newInstance(cls);这两个方法都是通过反射来
             * 实例化一个对象的，那么这两个方法有什么区别呢
             *
             * schema.newMessage()是通过Java的反射来创建对象的，一般是通过构造方法
             *
             * objenesis.newInstance(cls)这种创建对象的方法是不使用构造器的，不需要类提供构造器
             * 使用objenesis.newInstance(cls)的原因就是防止类没有提供构造器，或者类提供的构造器
             * 容易产生一些其他问题，使用这个方法使我们编写的工具类更加通用
             *
             * */

            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            cachedSchema.put(cls, schema);
        }
        return schema;
    }

}
