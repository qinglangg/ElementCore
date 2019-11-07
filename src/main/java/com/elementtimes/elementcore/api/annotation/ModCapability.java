package com.elementtimes.elementcore.api.annotation;

import net.minecraftforge.common.capabilities.Capability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Capability 能力系统
 * 该注解应当注解在能力的接口上
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModCapability {

    /**
     * Capability.IStorage 实现类
     */
    Class<? extends Capability.IStorage> value();

    /**
     * 优先选择的默认实现对象的创建方法
     * 若无此注解，则选择被 ModCapability 注解的返回 storageClass 类型的对象的无参静态方法
     * 若无符合要求的方法，则不会注册
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Factory {}
}
