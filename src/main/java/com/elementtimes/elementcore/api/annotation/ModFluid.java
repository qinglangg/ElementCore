package com.elementtimes.elementcore.api.annotation;

import net.minecraft.block.FlowingFluidBlock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 流体注册
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModFluid {

    String value() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface BurningTime {
        /**
         * @return 燃烧时间，包括桶和瓶
         */
        int value();
    }

    /**
     * 流体对应物品的 ItemGroup
     * @deprecated 暂时还没想好怎么合并
     */
    @Deprecated
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ItemGroup {
        /**
         * 流体对应物品的 ItemGroup
         * @return ItemGroup
         */
        String value() default "";
    }

    /**
     * 流体的 FluidTags
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Tags {
        /**
         * 流体对应物品的 FluidTags
         * @return FluidTags
         */
        String[] value() default "";
    }

    /**
     * 流体的 FlowingFluidBlock
     * 忽略则使用 FlowingFluidBlock
     * 构造函数接受一个 Fluid 参数
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Block {
        /**
         * 流体对应物品的 FluidTags
         * @return FluidTags
         */
        Class<? extends FlowingFluidBlock> value();
    }

    /**
     * 气体
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Gas { }
}
