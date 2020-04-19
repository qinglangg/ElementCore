package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import net.minecraft.world.gen.GenerationStage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表一个方块材质
 * @see net.minecraft.world.gen.feature.ConfiguredFeature
 * @see net.minecraft.world.biome.Biome#createDecoratedFeature(net.minecraft.world.gen.feature.Feature,
 *                                                             net.minecraft.world.gen.feature.IFeatureConfig,
 *                                                             net.minecraft.world.gen.placement.Placement,
 *                                                             net.minecraft.world.gen.placement.IPlacementConfig)
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Feature {

    Biome biome();

    GenerationStage.Decoration decoration();

    ValueType type() default ValueType.VALUE;

    /**
     * 返回 {@link net.minecraft.world.gen.feature.ConfiguredFeature} 对象
     */
    Getter object() default @Getter;

    /**
     * 参数
     *  {@link net.minecraft.block.Block}
     * 返回值
     *  {@link net.minecraft.world.gen.feature.ConfiguredFeature} 对象
     */
    Method method() default @Method;

    /**
     * 生成类型
     * @see net.minecraft.world.gen.feature.Feature
     */
    Getter feature() default @Getter;

    /**
     * 生成方法
     * 参数
     *  {@link net.minecraft.block.Block}
     *  {@link net.minecraft.world.gen.feature.Feature}
     * 返回值
     *  {@link net.minecraft.world.gen.feature.IFeatureConfig}
     * 默认值为主世界铁矿生成
     */
    Method featureConfig() default @Method;

    /**
     * 放置位置
     * @see net.minecraft.world.gen.placement.Placement
     */
    Getter placement() default @Getter;

    /**
     * 位置设置
     * 参数
     *  {@link net.minecraft.block.Block}
     *  {@link net.minecraft.world.gen.placement.Placement}
     * 返回值
     *  {@link net.minecraft.world.gen.placement.IPlacementConfig}
     * 默认值为主世界铁矿生成
     */
    Method placementConfig() default @Method;
}
