package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.EntitySpawn;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import com.elementtimes.elementcore.api.annotation.part.ItemProps;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TileEntityType 注册
 * 该注解可被应用到 Entity 类上，根据 Entity 类生成 EntityType 对象
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModEntity {

    /**
     * 实体 id
     * 默认使用 [modid].[className]
     */
    String id() default "";
    float width() default 0.6f;
    float height() default 1.8f;

    /**
     * 实体生成方法
     * 参数: EntityType, World
     * 返回值: Entity
     */
    Method create();
    EntityClassification classification() default EntityClassification.CREATURE;

    /**
     * 实体特性相关
     */
    boolean disableSummoning() default false;
    boolean disableSerialization() default false;
    boolean immuneToFire() default false;

    /**
     * 半径, 即超过该半径, 实体就不更新了
     *  -1 为默认，详见 {@link EntityType#defaultTrackingRangeSupplier()}
     */
    @SuppressWarnings("JavadocReference")
    int trackerRange() default -1;

    /**
     * 更新频率 单位 gametick, MAX_VALUE 为 不更新
     *  -1 为默认，详见 {@link EntityType#defaultUpdateIntervalSupplier()}
     */
    @SuppressWarnings("JavadocReference")
    int updateInterval() default -1;

    /**
     * 客户端实体生成
     * 参数：
     *  FMLPlayMessages.SpawnEntity
     *  World
     * 返回值：
     *  Entity
     */
    Method2 clientFactory() default @Method2;

    /**
     * 获取该实体的渲染类的创建方法
     * 参数
     *  EntityRendererManager
     * 返回值
     *  EntityRenderer
     */
    Method2 renderer();

    /**
     * 若直接注册 EntityType 实例，使用此注解
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Type {
        /**
         * RegistryName
         */
        String value() default "";
    }

    /**
     * 生物生成
     * 该注解应配合 ModEntity 或 ModEntity.Type 注解使用
     * 若不与这两个注解配合使用，应当重写 getter 属性，返回一个 EntityType 对象
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface Spawn {
        EntitySpawn[] value();

        /**
         * 若被注解对象没有被 ModEntity 或 ModEntity.Type 注解注册，使用该属性获取一个 EntityType 对象
         */
        Getter getter() default @Getter;
    }

    /**
     * 为该实体创建一个怪物蛋
     * 该注解应配合 ModEntity 注解使用，或注解到一个 EntityType 对象上
     * 若不与这个注解配合使用且注解到一个 Entity 类上，应当重写 getter 属性，返回一个 EntityType 对象
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface Egg {

        /**
         * RegistryName
         */
        String name() default "";

        int primary() default 0x00000000;
        int secondary() default 0x00000000;
        ItemProps prop() default @ItemProps;

        /**
         * 若被注解对象没有被 ModEntity 或 ModEntity.Type 注解注册，使用该属性获取一个 EntityType 对象
         */
        Getter getter() default @Getter;
    }
}
