///**
// * Alipay.com Inc.
// * Copyright (c) 2004-2019 All Rights Reserved.
// */
//package com.alipay.test.acts.playback.util;
//
///*-
// * #%L
// * loadup-components-test
// * %%
// * Copyright (C) 2022 - 2025 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.io.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author qingqin
// * @version $Id: util.java, v 0.1 2019年07月30日 下午10:11 qingqin Exp $
// */
//public class SerializeUtil {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeUtil.class);
//
//    /** hessian序列化工厂 */
//    final private static SerializerFactory        serializerFactory        = new SerializerFactory();
//
//    /** hessian-generic object序列化工厂 */
//    final private static GenericSerializerFactory genericSerializerFactory = new GenericSerializerFactory();
//
//    static {
//        serializerFactory.addFactory(new AliEnumSerializerFactory());
//        serializerFactory.setAllowNonSerializable(true);
//
//        genericSerializerFactory.addFactory(new AliEnumSerializerFactory());
//        genericSerializerFactory.setAllowNonSerializable(true);
//    }
//
//    /**
//     * 使一个对象泛化，去平台依赖
//     */
//    public Object generalized(Object o) {
//        return GenericUtils.convertToGenericObject(o);
//    }
//
//    /**
//     * 使一个对象去泛化
//     * 注意这里没有使用原先的泛化工具
//     * 主要是针对StackTrace进行了处理
//     */
//    public static Object deGeneralized(Object o) {
//        return HessianGenericUtils.convertToObject(o);
//    }
//
//    /**
//     * 反序列化为泛化对象
//     */
//    public static Object deserializeGeneric(byte[] bytes) throws IOException {
//        return deserializeInternal(bytes, genericSerializerFactory);
//    }
//
//    /**
//     * 反序列化为泛化对象
//     */
//    public static Object deserializeGenericFromString(String base64Str) throws IOException {
//        return deserializeGeneric(stringToByte(base64Str));
//    }
//
//    /**
//     * 序列化，并使用base64编码
//     */
//    public static String serializeToString(Object o) throws IOException {
//        return byteToString(serialize(o));
//    }
//
//    /**
//     * 序列化对象
//     */
//    public static byte[] serialize(Object o) throws IOException {
//        return serializeInternal(o, serializerFactory);
//    }
//
//    /**
//     * 序列化到指定流
//     */
//    public void serializeTo(Object o, OutputStream stream) throws IOException {
//        serializeTo(o, serializerFactory, stream);
//    }
//
//    /**
//     * 基于载入泛化序列化的工厂序列化到指定流
//     */
//    public void serializeGenericTo(Object o, OutputStream stream) throws IOException {
//        serializeTo(o, genericSerializerFactory, stream);
//    }
//
//    /**
//     * 序列化到指定流
//     */
//    public static void serializeTo(Object o, SerializerFactory factory, OutputStream stream)
//            throws IOException {
//        Hessian2Output output;
//
//        try {
//            output = new Hessian2Output(stream);
//            output.setSerializerFactory(factory);
//            output.writeObject(o);
//            output.flush();
//        } finally {
//            IOUtils.closeQuietly(stream);
//        }
//    }
//
//    /**
//     * 反序列化字段
//     */
//    public Object deserialize(byte[] bytes) throws IOException {
//        return deserializeInternal(bytes, serializerFactory);
//    }
//
//    /**
//     * 转化为base64编码
//     */
//    public static String byteToString(byte[] bytes) throws UnsupportedEncodingException {
//        if (bytes == null || bytes.length == 0) {
//            return null;
//        }
//
//        return new String(Base64.encodeBase64(bytes), "UTF-8");
//
//    }
//
//    /**
//     * 从base64编码转化
//     */
//    public static byte[] stringToByte(String base64Str) {
//        try {
//            return Base64.decodeBase64(base64Str.getBytes());
//        } catch (Throwable e) {
//            LoggerUtil.error(LOGGER, e, "base64解码失败");
//            return null;
//        }
//    }
//
//    /**
//     * 序列化内部包装
//     */
//    private static byte[] serializeInternal(Object o, SerializerFactory serializerFactory)
//            throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        serializeTo(o, serializerFactory, bos);
//        return bos.toByteArray();
//    }
//
//    /**
//     * 反序列化内部包装
//     */
//    private static Object deserializeInternal(byte[] bytes, SerializerFactory serializerFactory)
//            throws IOException {
//        if (bytes == null) {
//            return null;
//        }
//
//        ByteArrayInputStream ins = null;
//        Hessian2Input input;
//
//        try {
//            ins = new ByteArrayInputStream(bytes);
//            input = new Hessian2Input(ins);
//            input.setSerializerFactory(serializerFactory);
//
//            return input.readObject();
//        } finally {
//            IOUtils.closeQuietly(ins);
//        }
//    }
//
//    /**
//     * 自定义的AliEnumSerializerFactory
//     */
//    private static class AliEnumSerializerFactory extends AbstractSerializerFactory {
//        /**
//         * ALIENUM类缓存
//         */
//        private final ConcurrentHashMap<ClassLoader, Class<?>> loader2EnumClass = new ConcurrentHashMap<ClassLoader, Class<?>>();
//
//        /**
//         * @see AbstractSerializerFactory#getSerializer(Class)
//         */
//        public Serializer getSerializer(Class cl) throws HessianProtocolException {
//            if (isAlibabaEnum(cl, Thread.currentThread().getContextClassLoader())) {
//                return new AliEnumSerializer(cl);
//            }
//            return null;
//        }
//
//        /**
//         * @see AbstractSerializerFactory#getDeserializer(Class)
//         */
//        public Deserializer getDeserializer(Class cl) throws HessianProtocolException {
//            if (isAlibabaEnum(cl, Thread.currentThread().getContextClassLoader())) {
//                return new AliEnumDeserializer(cl);
//            }
//            return null;
//        }
//
//        /**
//         * 判断是不是AliEnum
//         */
//        private boolean isAlibabaEnum(Class<?> clz, ClassLoader loader) {
//            try {
//                Class<?> alibabaEnum = getAlibabaEnumClass(loader);
//                return alibabaEnum.isAssignableFrom(clz);
//            } catch (ClassNotFoundException e) {
//                return false;
//            }
//        }
//
//        /**
//         * 获取AliEnum的class
//         */
//        private Class<?> getAlibabaEnumClass(ClassLoader loader) throws ClassNotFoundException {
//            Class<?> alibabaEnum = loader2EnumClass.get(loader);
//            if (alibabaEnum == null) {
//                alibabaEnum = loader.loadClass("com.alibaba.common.lang.enumeration.Enum");
//                loader2EnumClass.putIfAbsent(loader, alibabaEnum);
//            }
//            return alibabaEnum;
//        }
//    }
//
//
//}
