package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用注解，自动推断注解类型
 * 取代之前的 @ModElement
 * Block 类
 * @deprecated 暂未完成，可能会在之后的几个版本中逐步完善
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ModAuto {
    String value() default "";

}
