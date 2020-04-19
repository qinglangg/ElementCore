package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.enums.PotionBottleType;
import com.elementtimes.elementcore.api.annotation.part.PotionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 药水
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
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

    PotionType withType() default @PotionType;

    /**
     * 向创造模式物品栏增加药水
     * 会查找所有包含该药水的 PotionEffect
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Bottles {
        /**
         * CreativeTabs
         * @return CreativeTabs
         */
        String value() default "misc";

        PotionBottleType[] types() default {PotionBottleType.NORMAL};
    }
}
