package com.elementtimes.elementcore.api.annotation.tools;

import com.elementtimes.elementcore.api.annotation.part.BurnTime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物品燃烧时间
 * 可注解 Item, Block, Fluid
 * 当注解 Fluid 时，燃烧时间为对应桶的燃烧时间
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModBurnTime {
    /**
     * 默认物品燃烧时间
     * @return 燃烧时间
     */
    int value();

    /**
     * 对于 ItemStack 不同 metadata（damage） 和 nbt 数据时的燃烧时间
     * @return 燃烧时间
     */
    BurnTime[] sub() default {};
}
