package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraft.item.Rarity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于创建一个 Item.BlockProps
 * @see Parts#propertiesItem(Object, ECModElements)
 * @see net.minecraft.block.Block.Properties
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemProps {
    /**
     * 获取一个 Food 类，表示该w物品为食物
     * @return Food 类实例
     */
    Getter foodGetter() default @Getter;

    /**
     * 创建一个 Food 类，表示该物品为食物
     * @return Food 类构建器
     */
    Food food() default @Food;

    /**
     * 最大叠加数目，默认 64
     * @return 叠加数
     */
    int maxStackSize() default 64;

    /**
     * 设置最大耐久度
     * 使用该参数后，maxStackSize 将会强制设置成 1
     * -1 代表无耐久
     * @return 耐久度
     */
    int maxDamage() default -1;

    /**
     * 返回一个 Item 类实例，表示该物品包含其他物品
     * 这意味着，当在合成表中合成使，会有物品保留
     * @return 包含物品
     */
    Getter containerItem() default @Getter;

    /**
     * 物品所在创造模式物品栏 key
     * @return 创造模式物品栏
     */
    Getter group() default @Getter;

    /**
     * 物品稀有度，默认 COMMON
     * @return 稀有度
     */
    Rarity rarity() default Rarity.COMMON;

    /**
     * 该物品是否不可修复，默认 false 即可修复
     * @return 不可修复
     */
    boolean noRepair() default false;

    /**
     * 该物品是否为一种工具
     * @return 工具类型及等级
     */
    ToolType[] toolType() default {};

    /**
     * 该物品是否具有 {@link net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer}
     * 需要返回一个 ItemStackTileEntityRenderer 对象
     * @return TEISR 获取方法
     */
    Getter2 teisr() default @Getter2;
}
