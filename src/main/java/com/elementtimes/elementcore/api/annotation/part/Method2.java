package com.elementtimes.elementcore.api.annotation.part;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于 OnlyIn(CLIENT) 的 Method 注解
 * 不传入 Class 对象防止误加载
 * 其他与 {@link Method} 相同
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Method2 {

    String value() default "";

    String name() default "<init>";

    boolean isStatic() default true;

    Getter2 holder() default @Getter2;
}
