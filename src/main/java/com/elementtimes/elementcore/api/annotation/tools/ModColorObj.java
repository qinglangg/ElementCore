package com.elementtimes.elementcore.api.annotation.tools;

import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Getter2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解到任意 Item/Block 对象中，作为方块/物品染色
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModColorObj {

    /**
     * 代表一个 IBlockColor 对象
     */
    Getter2 block() default @Getter2;

    /**
     * 代表一个 IItemColor 对象
     */
    Getter2 item() default @Getter2;
}
