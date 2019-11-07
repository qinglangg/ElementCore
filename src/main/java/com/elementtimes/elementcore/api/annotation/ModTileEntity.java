package com.elementtimes.elementcore.api.annotation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 与 TileEntityType
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModTileEntity {

    /**
     * 注解到 Block 对象上
     * TileEntityType#create 方法创建规则：
     *  1. 搜索 TileEntity 中所有静态方法，选取被 TileEntityCreator 注解的 ()TileEntity 方法
     *  2. 搜索注册的 Block 对象类中所有静态方法，选取被 TileEntityCreator 注解的 ()TileEntity 方法
     *  3. 搜索 TileEntity 中所有静态方法，选取 ()TileEntity 方法
     *  4. 搜索注册的 Block 对象类中所有静态方法，选取 ()TileEntity 方法
     *  5. 若 Block 实现 {@link net.minecraft.block.ITileEntityProvider}，选取 createNewTileEntity 方法
     *      若返回 null，则反射使用无参构造创建 TileEntity
     *  6. 选取 {@link net.minecraft.block.Block#createTileEntity(BlockState, IBlockReader)} 方法
     *      BlockState = {@link Block#getDefaultState()}
     *      若返回 null，则反射使用无参构造创建 TileEntity
     * 原本 TileEntity 注册是并入在 ModBlock 中的，然而新版本 mc 对 TileEntity 进行了单独注册
     * RegisterName 采用 {@link Block#getRegistryName()}，若为空则使用成员名
     * @return TileEntity 类
     */
    Class<? extends net.minecraft.tileentity.TileEntity> value();

    /**
     * 注册优先使用的 TileEntityType#create 方法
     * 注解 Block 或 TileEntity 的方法
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface TileEntityCreator { }

    /**
     * 注册 TileEntityType
     * 注解 TileEntityType 对象
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface TileEntityType {
        /**
         * RegisterName
         * 当留空则默认使用成员名小写
         * @return RegisterName
         */
        String value() default "";
    }
}
