package com.elementtimes.elementcore.api.annotation.tools;

import com.elementtimes.elementcore.api.annotation.part.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物品栏物品检索编辑
 * 可注解到要修改的 CreativeTabs 对象上
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModTabEditor {

    /**
     * 参数
     *  NonNullList<ItemStack>：该 CreativeTabs 中的所有物品
     * 返回值
     *  无
     * @return 物品修改器
     */
    Method value();
}
