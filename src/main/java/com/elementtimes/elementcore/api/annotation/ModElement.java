package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 以类为单位的自动注册，只注册静态成员
 * 支持类型：
 *  Block, TileEntityType,
 *  ContainerType
 *  (Container, PlayerInventory, ITextComponent)Screen 方法
 *      注册时首先搜索类中同名 ContainerType 对象，若不存在则使用 [ModId]:[MethodName] 对应的 ContainerType 对象
 *  IScreenFactory
 *      注册时使用 [ModId]:[fieldName] 对应的 ContainerType 对象
 *  Item
 *  Enchantment, FlowingFluid, ItemGroup, IRecipeSerializer
 * @author luqin2007
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModElement {
//    /**
//     * 允许检查方法。
//     * 若该值为 true，则除了遍历 Field 外，还将遍历 Method
//     * @return 遍历方法
//     */
//    boolean enableMethod() default false;

    /**
     * 跳过注册
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface Skip {}
}
