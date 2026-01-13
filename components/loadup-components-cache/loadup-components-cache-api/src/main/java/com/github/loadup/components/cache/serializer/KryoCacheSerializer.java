package com.github.loadup.components.cache.serializer;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoCacheSerializer implements CacheSerializer {

    // Kryo 实例是非线程安全的，使用 ThreadLocal 保证线程安全
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false); // 允许序列化未注册的类
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) return new byte[0];
        Kryo kryo = kryoThreadLocal.get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, obj);
        output.close();
        return outputStream.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0) return null;
        Kryo kryo = kryoThreadLocal.get();
        Input input = new Input(new ByteArrayInputStream(bytes));
        return (T) kryo.readClassAndObject(input);
    }
}
