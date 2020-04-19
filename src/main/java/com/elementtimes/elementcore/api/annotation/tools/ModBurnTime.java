package com.elementtimes.elementcore.api.annotation.tools;

import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物品燃烧时间
 * 根据被注解内容增加 Predicate
 *  IItemProvider   只应用于持有该物品的 ItemStack
 *  Fluid           只应用于存有该流体的 ItemStack
 *  EntityType      只应用于包含该实体的 ItemStack
 *  Enchantment     只应用被该附魔附魔的 ItemStack
 *  Class           若该类为上面某一类的子类，类似上面的判断
 *      Entity      尝试使用不可知的 world (服务端iterator迭代的第一个) 实例化该实体，并对其判断
 *  其他             应用全部 ItemStack
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ModBurnTime {
    /**
     * 燃烧时间获取方法
     * ValueType.NONE   不使用，直接使用默认值
     */
    ValueType type() default ValueType.VALUE;

    /**
     * 燃烧时间
     */
    int value() default 0;

    /**
     * 获取一个 Integer
     * 用处不大，为了枚举都有用吧
     */
    Getter object() default @Getter();

    /**
     * 参数
     *  FurnaceFuelBurnTimeEvent
     * 返回值
     *  int
     * f返回值小于 0 表示使用默认值
     */
    Method method() default @Method;
}
