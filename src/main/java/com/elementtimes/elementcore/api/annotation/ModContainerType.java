package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册 ContainerType
 * 可注解 ContainerType 对象
 *
 * 要注册一个 GUI，需要一个 ContainerType，一个 Container，一个 Screen
 *  ContainerType 用于判断 GUI 类型，内含 Container 的创建方法
 *  Container 为 Gui 内部逻辑的处理对象
 *  Screen 为 Gui 图像/文本等渲染对象
 *      在使用 ModContainerType/ContainerFactory 注解时，会自动搜索符合条件的静态方法注册
 *          同名静态 (Container, PlayerInventory, ITextComponent)Screen 方法
 *      可使用 ScreenCreator/ScreenFactory 注解手动指定
 * @see net.minecraft.inventory.container.ContainerType
 * @see net.minecraft.inventory.container.Container
 * @see net.minecraft.client.gui.screen.Screen
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ModContainerType {
    /**
     * RegisterName
     * 当留空则默认使用成员名小写
     * @return RegisterName
     */
    String value() default "";

    /**
     * 注册 ContainerType
     * 可注解静态 (int, PlayerInventory)Container 方法
     * 可注解静态 (Integer, PlayerInventory)Container 方法
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ContainerFactory {
        /**
         * RegisterName
         * 当留空则默认使用成员名(即方法名)小写
         * @return RegisterName
         */
        String value() default "";
    }

    /**
     * 为 ContainerType 注册 GUI
     * 注解静态方法：(Container, PlayerInventory, ITextComponent)Screen
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ScreenCreator {
        /**
         * 被注册的 ContainerType 的 RegisterName
         * 若留空，则先会搜索类中同名 ContainerType 类型对象，若不存在则使用 [ModId]:[MethodName] 注册的 ContainerType
         * @return ContainerType 的 RegisterName
         */
        String value() default "";
    }

    /**
     * 为 ContainerType 注册 GUI
     * 注解 IScreenFactory 对象
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface ScreenFactory {
        /**
         * 被注册的 ContainerType 的 RegisterName
         * 留空则为 [ModId]:[MethodName]
         * @return ContainerType 的 RegisterName
         */
        String value() default "";
    }
}
