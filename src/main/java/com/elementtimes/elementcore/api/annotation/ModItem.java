package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Color;
import com.elementtimes.elementcore.api.annotation.part.ItemProps;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物品
 * 当应用于一个类时，该类应当继承自 Item，且存在一个无参构造
 * @see net.minecraft.item.Item
 * @see net.minecraft.item.Item.Properties
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ModItem {

    /**
     * RegistryName
     */
    String value() default "";

    Color color() default @Color;
}