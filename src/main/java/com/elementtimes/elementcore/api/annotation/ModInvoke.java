package com.elementtimes.elementcore.api.annotation;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 调用某静态方法
 * 可以注解 ()V 或 (Event)V 方法
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModInvoke {
    /**
     * 调用事件
     * @return 事件
     */
    Class<? extends Event> value() default FMLCommonSetupEvent.class;
}