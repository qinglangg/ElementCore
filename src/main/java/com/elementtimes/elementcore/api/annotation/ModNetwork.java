package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务端与客户端通信
 * 注解在数据类中
 * 只有能查找到分别符合
 *  (MSG, PacketBuffer)V
 *  (PacketBuffer)MSG
 *  (MSG, Supplier)V
 * 三个签名的静态方法才能生效
 * 查找路径为当前类和 classes 中的类
 * 优先选择具有 Encoder/Decoder/Consumer 注解的方法
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModNetwork {

    /**
     * 搜索的类
     * @return 类
     */
    String[] value() default "";

    /**
     * 将数据类存储到 PacketBuffer 中
     * 方法签名 (MSG, PacketBuffer)V
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Encoder { }

    /**
     * 从 PacketBuffer 读取数据类
     * 方法签名 (PacketBuffer)MSG
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Decoder { }

    /**
     * 客户端对数据类的处理
     * 方法签名 (MSG, Supplier<NetworkEvent.Context>)V
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Consumer { }
}
