package com.elementtimes.elementcore.annotation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义物品的基本信息
 * 包括 registerName，unlocalizedName，creativeTab
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ModItem {
    /**
     * RegisterName，代表物品注册名
     * 当该注解注解 Field 且物品 registerName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * 当该注解注解 Class 且物品 registerName 与类名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String registerName() default "";

    /**
     * 物品染色
     * 只要物品材质直接或间接继承自 item/generated 就能支持染色
     * @return 物品染色所需 IItemColor 类全类名
     */
    String itemColorClass() default "";
}