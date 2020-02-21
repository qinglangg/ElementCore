package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注册 CreativeTabs
 * 可将其注解到静态变量中。
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModTab {
    /**
     * 注册 id
     * 若留空，则使用变量名
     * @return id
     */
    String value() default "";
}
