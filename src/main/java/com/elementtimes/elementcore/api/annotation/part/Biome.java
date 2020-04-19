package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.annotation.enums.ValueType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表一个方块材质
 * 该注解返回一个 {@link java.util.function.Supplier<net.minecraft.world.biome.Biome>}
 * @see net.minecraft.world.biome.Biome
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Biome {

    ValueType type() default ValueType.VALUE;

    Getter object() default @Getter;

    /**
     * 参数
     *  {@link net.minecraft.entity.EntityType}
     *  或
     *  {@link net.minecraft.block.Block}
     * 返回值
     *  {@link net.minecraft.world.biome.Biome}
     */
    Method method() default @Method;

    /**
     * 由此创建 ResourceLocation 寻找 Biome
     * @see net.minecraftforge.registries.ForgeRegistries#BIOMES
     */
    String value() default "";
}
