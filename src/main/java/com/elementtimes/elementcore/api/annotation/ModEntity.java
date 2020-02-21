package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Method2;
import net.minecraft.entity.EnumCreatureType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModEntity {

    int network() default 2;
    String id();
    String name();

    /**
     * 实体跟踪相关
     * trackerRange 半径, 即超过该半径, 实体就不更新了 一般生物 64 比较合适
     * updateFrequency 更新频率 单位 gametick 一般生物 3 比较合适, MAX_VALUE 为 不更新
     * sendVelocityUpdate 是否同步实体的速度更新, 静止实体和手动更新位置的实体为 false
     */
    int trackerRange() default 64;
    int updateFrequency() default 3;
    boolean sendVelocityUpdate() default true;

    /**
     * 怪物蛋相关
     * hasEgg 是否添加怪物蛋
     * eggColorPrimary 怪物蛋主要颜色
     * eggColorSecondary 怪物蛋次要颜色
     */
    boolean hasEgg() default true;
    int eggColorPrimary() default 0x000000;
    int eggColorSecondary() default 0x000000;

    /**
     * 世界生成相关
     * canSpawn 是否生成
     * spawnType 生物类型
     * spawnWeight 优先权重
     * spawnMin 最少生成量
     * spawnMax 最大生成量
     * biomeIds 生成生物群系
     */
    boolean canSpawn() default false;
    EnumCreatureType spawnType() default EnumCreatureType.CREATURE;
    int spawnWeight() default 0; // weight 权重越高越可能优先生成
    int spawnMin() default 0;
    int spawnMax() default 0;
    String[] biomeIds() default { "plains" };

    /**
     * 渲染对象
     * 参数
     *  RenderManager
     * 返回值
     *  <T extend Entity> Render<? super T>
     */
    Method2 render() default @Method2;
}
