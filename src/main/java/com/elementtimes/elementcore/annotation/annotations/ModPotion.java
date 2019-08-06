package com.elementtimes.elementcore.annotation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 药水
 * 未启用
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface ModPotion {
    String registerName();
    String name();
    String creativeTabClass() default "com.elementtimes.tutorial.common.creativetabs.ElementTimesTabs$Main";
}
