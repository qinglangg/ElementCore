package com.elementtimes.elementcore.api.annotation;

import net.minecraft.client.renderer.color.IItemColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义物品的基本信息
 * 包括 registerName，unlocalizedName，creativeTab
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModItem {
    /**
     * RegisterName，代表物品注册名
     * 当该注解注解 Field 且物品 registerName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * 当该注解注解 Class 且物品 registerName 与类名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String value() default "";

    /**
     * 着色器
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ItemColor {
        /**
         * 物品染色
         * 只要物品材质直接或间接继承自 item/generated 就能支持染色
         * @return 物品染色所需 IItemColor 类全类名
         */
        Class<? extends IItemColor> value();
    }

    /**
     * 自定义 ItemStack 的 Tooltips
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tooltips {
        /**
         * Tooltips
         * 使用 @n 表示访问 NBT，@c 表示访问个数，-> 表示匹配检查
         *  "@c2@n{Tag}=3->stack3" 意味着只有在 count=2, getTagCompound中Tag值为3 时才会添加 "stack3" 字符串
         *  "stack c=@c, nbtName=@n{Stack.Name}" 表示添加
         *      "stack c=[count 值], nbtName=[getTag().getTag("Stack").getTag("Name") 不存在则为 null]"
         * 可以有多个相同类型匹配，同一类型中默认匹配方式为 或，@n& 表示 NBT且，不同类型间使用 且
         *  "@n{Tag}=3@n{Tag}=5@n&{Name}=aa->..." 表示 (Tag="3" || Tag="5" && Name="aa") 时匹配通过
         * @return 方块/物品 Tooltip
         */
        String[] value();
    }

    /**
     * 为 Block 注册一个或多个矿辞（BlockTags）
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tags {
        /**
         * 注册矿辞
         * 若留空，则使用 forge:[成员名] 作为矿辞
         * @return 所有矿辞
         */
        String[] value() default "";
    }

    /**
     * 燃烧时间
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface BurningTime {
        /**
         * 燃烧时间
         * @return 燃烧时间
         */
        int value();
    }
}