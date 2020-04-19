package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.Vanilla;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import net.minecraft.block.Block;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于创建一个 {@link net.minecraft.block.Block.Properties}
 * @see Parts#propertiesBlock(Object, Block, ECModElements)
 * @see net.minecraft.block.Block.Properties
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface BlockProps {

    ValueType type() default ValueType.OBJECT;

    /**
     * 返回一个 Block 或 Block.BlockProps 对象
     * 当返回 Block 对象时，使用 {@link net.minecraft.block.Block.Properties#from(Block)} 方法获取
     */
    Getter object() default @Getter;

    /**
     * 参数
     *  无
     * 返回值
     *  {@link net.minecraft.block.Block} 或 {@link net.minecraft.block.Block.Properties}
     * 当返回 Block 对象时，使用 {@link net.minecraft.block.Block.Properties#from(Block)} 方法获取
     */
    Method method() default @Method;

    Material material() default @Material;

    int colorIndex() default -1;

    int colorDye() default -1;

    boolean doesNotBlockMovement() default false;

    float slipperiness() default 0.6f;

    Getter soundType() default @Getter(value = Vanilla.Sounds.class, name = "STONE");

    int lightValue() default 0;

    float hardness() default 0f;

    float resistance() default 0f;

    boolean ticksRandomly() default false;

    boolean variableOpacity() default false;

    ToolType harvest() default @ToolType(tool = @Getter, level = -1);

    boolean noDrops() default false;

    /**
     * 返回一个 Block 对象，使用该 Block 的掉落物
     */
    Getter loot() default @Getter;
}
