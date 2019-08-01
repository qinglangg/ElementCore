package com.elementtimes.elementcore.annotation.annotations;

import net.minecraft.fluid.Fluid;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 流体注册
 * 注解到 {@link com.elementtimes.elementcore.fluid.Fluid.FluidResult} 或 {@link net.minecraft.fluid.Fluid} 对象上
 * 对应 {@link net.minecraftforge.fluids.Fluid} 对象通过 {@link com.elementtimes.elementcore.fluid.FluidUtil#fromFluid(Fluid)} 获取
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModFluid {
    int burningTime() default -1;
}
