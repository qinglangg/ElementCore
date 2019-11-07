package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注册 CreativeTabs
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModItemGroup {
    /**
     * 注册 id
     * 若留空，则使用变量名，使用 toLowerCase 处理
     * @return id
     */
    String value() default "";
}
