package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Getter2;
import com.elementtimes.elementcore.api.annotation.part.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册 TileEntityType
 * 可应用到一个 TileEntityType 实例或 TileEntity 类上
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ModTileEntity {

    /**
     * RegistryName
     */
    String name() default "";

    /**
     * 当该注解应用到一个 TileEntity 类上时，会使用该参数设置 te 应用到的方块
     */
    Getter[] blocks() default {};

    /**
     * 当该注解应用到一个 TileEntity 类上时，使用该方法指定 te 的创建方法
     * 若使用 TileEntity 的仅有一个 {@link net.minecraft.tileentity.TileEntityType} 参数的构造函数，该属性可忽略
     * 参数
     *  {@link net.minecraft.tileentity.TileEntityType}
     * 返回值
     *  {@link net.minecraft.tileentity.TileEntity}
     */
    Method newTe() default @Method;

    /**
     * 为该 TileEntity 注册一个 TileEntityRenderer
     * 该注解应当被应用到一个 TileEntity 类上
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Ter {

        /**
         * 默认使用 TileEntityRendererAnimation 即ASM动画
         * @return TileEntityRenderer 类
         */
        Getter2 value() default @Getter2(value = "net.minecraftforge.client.model.animation.TileEntityRendererAnimation");
    }
}
