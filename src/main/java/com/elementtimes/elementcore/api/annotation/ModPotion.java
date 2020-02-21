package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 药水
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SuppressWarnings("unused")
public @interface ModPotion {

    /**
     * RegistryName
     * 默认 变量名
     * @return RegistryName
     */
    String value() default "";

    /**
     * PotionName
     * 默认 变量名
     * @return PotionName
     */
    String name() default "";
}
