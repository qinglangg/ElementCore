package com.elementtimes.elementcore.api.annotation.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 物品栏物品检索编辑
 * 可注解到修改 Tab 的方法上
 * 参数
 *  CreativeTabs, NonnullList<ItemStack>
 *      CreativeTabs 为在修改的 CreativeTabs
 * 返回值
 *  无
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ModTabEditorFunc { }
