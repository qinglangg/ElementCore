package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 客户端与服务端通信，注解到事件类
 * 类似 {@link net.minecraftforge.fml.common.Mod.EventBusSubscriber}，只是注册到 EventDrivenChannel
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModEventNetwork {

    /**
     * 是否注册实例
     * 当该值为 true 时，会使用无参构造实例化一个实例，注册该实例
     * 默认为 false，即注册被注解的类而非实例
     * @return 注册实例
     */
    boolean value() default false;
}
