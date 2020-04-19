package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Capability 能力系统
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModCapability {

    /**
     * Capability 对应数据接口类的实现类的获取方式
     * 参数
     *  无
     * 返回值
     *  实现了 type 属性指向的接口的实例
     */
    Method factory();

    /**
     * Capability.IStorage 实现类
     * 获取一个 Capability.IStorage 实例
     */
    Getter storage();
}
