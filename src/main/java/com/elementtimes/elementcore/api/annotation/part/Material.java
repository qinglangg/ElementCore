package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.Vanilla;
import com.elementtimes.elementcore.api.annotation.enums.ValueType;
import net.minecraft.block.material.PushReaction;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表一个方块材质
 * 返回一个 {@link java.util.Optional<net.minecraft.block.material.Material>}
 * @see net.minecraft.block.material.Material
 * @see Parts#material(Object, net.minecraft.block.Block, ECModElements)
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Material {

    ValueType type() default ValueType.OBJECT;

    Getter object() default @Getter;

    /**
     * 参数
     *  无
     * 返回值
     *  {@link net.minecraft.block.material.Material}
     */
    Method method() default @Method;

    /**
     * 可选范围 0-63
     *  0: AIR                  , color=0
     *  1: GRASS                , color=8368696
     *  2: SAND                 , color=16247203
     *  3: WOOL                 , color=13092807
     *  4: TNT                  , color=16711680
     *  5: ICE                  , color=10526975
     *  6: IRON                 , color=10987431
     *  7: FOLIAGE              , color=31744
     *  8: SNOW                 , color=16777215
     *  9: CLAY                 , color=10791096
     * 10: DIRT                 , color=9923917
     * 11: STONE                , color=7368816
     * 12: WATER                , color=4210943
     * 13: WOOD                 , color=9402184
     * 14: QUARTZ               , color=16776437
     * 15: ADOBE                , color=14188339
     * 16: MAGENTA              , color=11685080
     * 17: LIGHT_BLUE           , color=6724056
     * 18: YELLOW               , color=15066419
     * 19: LIME                 , color=8375321
     * 20: PINK                 , color=15892389
     * 21: GRAY                 , color=5000268
     * 22: LIGHT_GRAY           , color=10066329
     * 23: CYAN                 , color=5013401
     * 24: PURPLE               , color=8339378
     * 25: BLUE                 , color=3361970
     * 26: BROWN                , color=6704179
     * 27: GREEN                , color=6717235
     * 28: RED                  , color=10040115
     * 29: BLACK                , color=1644825
     * 30: GOLD                 , color=16445005
     * 31: DIAMOND              , color=6085589
     * 32: LAPIS                , color=4882687
     * 33: EMERALD              , color=55610
     * 34: OBSIDIAN             , color=8476209
     * 35: NETHERRACK           , color=7340544
     * 36: WHITE_TERRACOTTA     , color=13742497
     * 37: ORANGE_TERRACOTTA    , color=10441252
     * 38: MAGENTA_TERRACOTTA   , color=9787244
     * 39: LIGHT_BLUE_TERRACOTTA, color=7367818
     * 40: YELLOW_TERRACOTTA    , color=12223780
     * 41: LIME_TERRACOTTA      , color=6780213
     * 42: PINK_TERRACOTTA      , color=10505550
     * 43: GRAY_TERRACOTTA      , color=3746083
     * 44: LIGHT_GRAY_TERRACOTTA, color=8874850
     * 45: CYAN_TERRACOTTA      , color=5725276
     * 46: PURPLE_TERRACOTTA    , color=8014168
     * 47: BLUE_TERRACOTTA      , color=4996700
     * 48: BROWN_TERRACOTTA     , color=4993571
     * 49: GREEN_TERRACOTTA     , color=5001770
     * 50: RED_TERRACOTTA       , color=9321518
     * 51: BLACK_TERRACOTTA     , color=2430480
     * @return 颜色序号，用以获取 MaterialColor 类
     */
    int colorIndex() default 11;

    boolean isLiquid() default false;

    boolean isSolid() default true;

    boolean isBlockMovement() default true;

    boolean isOpaque() default true;

    boolean requiresTool() default false;

    boolean flammable() default false;

    boolean replaceable() default false;

    PushReaction pushReaction() default PushReaction.NORMAL;
}
