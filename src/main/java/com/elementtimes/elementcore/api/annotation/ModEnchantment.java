package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.enums.EnchantmentBook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 附魔注册
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModEnchantment {

    /**
     * @return registryName
     */
    String value() default "";

    /**
     * @return name
     */
    String name() default "";

    /**
     * 添加附魔书
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Book {

        /**
         * 若需要向创造模式物品栏中添加对应附魔书，修改此值
         * @return 附魔书类型
         */
        EnchantmentBook book() default EnchantmentBook.ALL;

        /**
         * 附魔书所在创造模式物品栏
         * @return
         */
        String creativeTabKey() default "tools";
    }
}