package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Method2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注册快捷键
 * 注解到 KeyBinding 对象上
 * @see net.minecraft.client.settings.KeyBinding
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModKey {

    /**
     * 绑定按键触发方法
     * 参数
     *  KeyInputEvent, KeyBinding
     * 返回值
     *  无
     * @return 方法
     */
    Method2 value() default @Method2;
}