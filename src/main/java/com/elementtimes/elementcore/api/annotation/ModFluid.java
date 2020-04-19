package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 流体注册
 * 注册到 Fluid 类型对象上
 * 若流体继承自 FlowingFluid，只需要应用于 Flowing 和 Source 类型的任意一个流体对象即可
 * @see net.minecraftforge.fluids.ForgeFlowingFluid
 * @see net.minecraftforge.fluids.ForgeFlowingFluid.Properties
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModFluid {
    /**
     * 非 FlowingFluid，或 source 类型流体
     */
    String name() default "";

    /**
     * flowing 类型流体
     */
    String flowingName() default "";

    /**
     * 若流体没有流体桶或流体桶由用户自己注册，则为 true
     */
    boolean noBucket() default false;
}
