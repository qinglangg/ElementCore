package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.enums.GenType;
import com.elementtimes.elementcore.api.annotation.enums.HarvestType;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Getter2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注册 Block
 * 请手动赋值，否则对其引用可能会出问题（编译器优化时会直接给他赋值为 null）
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModBlock {

    /**
     * RegisterName，代表方块注册名
     * 当该注解注解 Field 且方块 registerName 与变量名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String registerName() default "";

    /**
     * UnlocalizedName，用于获取方块显示名
     * 当 unlocalizedName 与变量名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return unlocalizedName
     */
    String unlocalizedName() default "";

    /**
     * @return 注册的创造模式标签
     */
    String creativeTabKey() default "";

    /**
     * 注册该类绑定的 TileEntity
     *  name：对应 TileEntity 注册名，留空则使用 Block.getRegistryName.getResourcePath
     *  value: 对应 TileEntity 类全类名
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.TYPE})
    @interface TileEntity {
        String name() default "";
        Class<? extends net.minecraft.tileentity.TileEntity> value();
    }

    /**
     * 相当于 setHarvestLevel 方法
     * 用于消灭 Block 类
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface HarvestLevel {
        HarvestType toolClass() default HarvestType.pickaxe;
        int level() default 2;
    }

    /**
     * 用于定义方块的世界生成
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface WorldGen {
        /**
         * @return 可生成高度范围
         */
        int YRange() default 48;

        /**
         * @return 最低生成高度
         */
        int YMin() default 16;

        /**
         * @return 矿物生成规模
         */
        int count() default 8;

        /**
         * @return 尝试生成次数
         */
        int times() default 6;

        /**
         * @return 每次尝试生成的成功率，范围 (0, 1)
         */
        float probability() default 0.6f;

        /**
         * @return 不生成的维度
         */
        int[] dimBlackList() default {};

        /**
         * @return 允许生成的维度。留空则不限制
         */
        int[] dimWhiteList() default {0};

        /**
         * @return 世界生成的种类
         */
        GenType type() default GenType.Ore;
    }

    /**
     * 用于自定义世界生成
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface WorldGenObj {

        /**
         * @return 自定义世界生成 WorldGenerator 对象
         */
        Getter value();

        /**
         * @return 世界生成的种类
         */
        GenType type() default GenType.Ore;
    }

    /**
     * 使用 StateMap
     * 指定材质应用于方块本体
     * @see StateMapper
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface StateMap {
        // suffix() 模型文件名的后缀
        String suffix() default "";
        // IProperty 属性名
        // withName() 模型 state 文件名的主体
        Getter2 name();
        // ignore() 模型 state 文件中 忽略的属性
        Getter2[] ignores() default {};
    }

    /**
     * 使用自定义 IStateMap
     * 指定材质应用于方块本体
     * @see StateMap
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface StateMapper {
        Getter2 value();
    }

    /**
     * 为该 Block 注册一个 TileEntitySpecialRenderer
     * 会覆盖 AnimTESR 注解
     * 由于 TESR 为 SideOnly(CLIENT)，故使用 String 而非 Class
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TESR {
        /**
         * TileEntitySpecialRenderer 类
         * @return TileEntitySpecialRenderer 类
         */
        String value();
    }

    /**
     * 为该 Block 注册一个 ASM
     * 由于 TESR 为 SideOnly(CLIENT)，故使用 String 而非 Class
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface AnimTESR {
        /**
         * 用于处理动画回调，复写 handleEvents 方法
         * 位于客户端，需要有一个无参构造
         * 留空 则为一个默认空实现
         * @return 该方块的 AnimationTESR 类全类名
         */
        String value() default "";
    }

    /**
     * 注册该类绑定的 TileEntity
     *  name：对应 TileEntity 注册名，留空则使用 Block.getRegistryName.getResourcePath
     *  value: 对应 TileEntity 类全类名
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface BlockColor {

        /**
         * 简单的方块染色。如果要更详细的设置，使用 ModColor 或 ModColorObj 注解
         * @return 方块染色
         */
        int value() default 0;

        /**
         * 简单的物品染色。如果要更详细的设置，使用 ModColor 或 ModColorObj 注解
         *  -1 代表无物品染色
         * @return 物品染色
         */
        int item() default 0;
    }

    /**
     * Tooltips
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tooltip {
        /**
         * 简单的 Tooltips 设置
         * 使用 {@link com.elementtimes.elementcore.api.annotation.tools.ModTooltip} 可根据物品栈详细修改 Tooltip
         * @return Tooltip
         */
        String[] value();
    }
}