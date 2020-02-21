package com.elementtimes.elementcore.api.annotation.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 某些通用设置
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModConfig {
    /**
     * 启用 OBJ 渲染
     */
    boolean useOBJ() default false;

    /**
     * 启用 U3D 渲染
     */
    boolean useB3D() default false;
}
