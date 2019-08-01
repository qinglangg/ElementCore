package com.elementtimes.elementcore.annotation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记注册 Block
 * 可将其注解到类或静态变量中。
 *  注解类则会使用无参构造实例化被标记类，并注册
 *  注解成员变量则会尝试使用无参构造实例化成员变量类型，并注册
 *  成员请手动赋值，否则对其引用可能会出问题（编译器优化时会直接给他赋值为 null）
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ModBlock {
    /**
     * RegisterName，代表方块注册名
     * 当该注解注解 Field 且方块 registerName 与属性名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * 当该注解注解 Class 且方块 registerName 与类名相同（忽略大小写，使用 toLowerCase 处理）时，可省略
     * @return registerName
     */
    String registerName() default "";

    /**
     * @return 燃烧时间
     */
    int burningTime() default 0;

    /**
     * 注册该类绑定的 TileEntity
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface TileEntity {
        /**
         * 注册名
         * @return 注册名
         */
        String name();

        /**
         * TileEntity 类
         * @return TileEntity 类
         */
        String clazz();

        /**
         * TE 创建方法
         * 应该是 TileEntity 类的一个静态方法
         * 该方法为无参方法，若默认留空，则会通过反射直接创建类，此时应具有一个无参构造
         * @return TE 创建方法名
         */
        String teCreator() default "";

        /**
         * TileEntityType 创建方法或其对象
         * 应当为一个 TileEntityType 类型的静态函数或静态成员，当留空时则根据 teCreator 创建
         * @return TileEntityType
         */
        String teType() default "";
    }
}