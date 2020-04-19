package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.Vanilla;
import com.elementtimes.elementcore.api.annotation.enums.EnchantmentBook;
import com.elementtimes.elementcore.api.annotation.part.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 附魔注册
 * 若注解到 {@link net.minecraft.enchantment.Enchantment} 类中，则该类应当有一个无参构造
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ModEnchantment {

    /**
     * @return registryName
     */
    String value() default "";

    /**
     * 添加附魔书
     * @deprecated 未实现
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Deprecated
    @interface Book {

        /**
         * 若需要向创造模式物品栏中添加对应附魔书，修改此值
         */
        EnchantmentBook value() default EnchantmentBook.ALL;

        /**
         * 附魔书所在创造模式物品栏
         */
        Getter groups() default @Getter(value = Vanilla.Groups.class, name = "TOOLS");
    }
}