package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import net.minecraft.item.Item;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于生成 {@link net.minecraft.item.Food} 类，以供 Item 初始化
 * @see Parts#food(Object, Item, ECModElements)
 * @see net.minecraft.item.Food
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Food {

    /**
     * 回复饱食度，-1 说明该 Food 为 null，仅供占位
     * @return 饱食度
     */
    int hunger() default -1;

    /**
     * 恢复饥饿值，即黄色小鸡腿
     * @return 饥饿值
     */
    float saturation() default 0f;

    /**
     * 是否为肉
     * @return 肉
     */
    boolean meat() default false;

    /**
     * 玩家不饿时，是否可以食用
     * @return 忽略饱食度检查
     */
    boolean alwaysEdible() default false;

    /**
     * 是否可以以比较快的速度使用
     * @return 快速食用
     */
    boolean fastToEat() default false;

    /**
     * 附加的药水效果
     * @return 药水效果
     */
    FoodEffect[] effect() default {};
}
