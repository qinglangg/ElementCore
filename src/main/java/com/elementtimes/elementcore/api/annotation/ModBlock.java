package com.elementtimes.elementcore.api.annotation;

import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraftforge.client.model.animation.TileEntityRendererAnimation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注册 Block
 * 可将其注解到静态变量中。
 *  注解成员变量则会尝试使用无参构造实例化成员变量类型，并注册
 *  成员请手动赋值，否则对其引用可能会出问题（编译器优化时会替换为 null）
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModBlock {
    /**
     * RegisterName，代表方块注册名
     * 当该注解注解 Field 且方块 registerName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String value() default "";

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

    /**
     * 方块对应物品的 ItemGroup
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ItemGroup {
        /**
         * 物品 ItemGroup
         * 留空则默认使用注册的 ItemGroup，用于当 Mod 只有一个创造物品栏时使用
         */
        String value() default "";
    }

    /**
     * 着色器
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface BlockColor {
        /**
         * 方块着色器类
         * @return 着色器
         */
        Class<? extends IBlockColor> value();
    }

    /**
     * 启用 OBJ 渲染
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface OBJ {}

    /**
     * 启用 U3D 渲染
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface B3D {}

    /**
     * 为该 TileEntity 注册一个 TileEntityRenderer
     * 注解到一个 TileEntity 类上
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TER {
        /**
         * TileEntityRenderer 类
         * @return TileEntityRenderer 类
         */
        Class<TileEntityRenderer> value();
    }

    /**
     * 为该 TileEntity 注册一个 ASM
     * 注解到一个 TileEntity 类上
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AnimTER {
        /**
         * 用于处理动画回调，复写 handleEvents 方法
         * 位于客户端，需要有一个无参构造
         * 留空 则为一个默认空实现
         * @return 该方块的 TileEntityRendererAnimation(原 AnimationTESR) 类
         */
        Class<? extends TileEntityRendererAnimation> value() default TileEntityRendererAnimation.class;
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

        /**
         * 是否为方块对应物品也添加对应矿辞
         * @return 为物品添加
         */
        boolean item() default true;
    }
}