package com.elementtimes.elementcore.api.annotation;

import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.annotation.part.Method2;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册 Container 和 ContainerScreen 对象
 * 该注解应当应用到 Container 类的相关内容中，因为 ContainerScreen 类往往是 ClientOnly 的
 *  若应用到 Container 类，则该类应当有一个可被使用的构造函数
 *  若应用到一个方法中，则该方法标志应当为一个可被使用的方法
 *  若应用到一个对象上，则该对象应当为 ContainerType 类型
 * 有关可被使用
 *  可被使用 是指 接受
 *      (int id, PlayerInventory inventory)
 *  或
 *      (int id, PlayerInventory inventory, PacketBuffer extraData)
 *  类型的参数的 Container 子类的构造函数或返回 Container 对象的方法
 *  （即 可直接转化为 {@link net.minecraft.inventory.container.ContainerType.IFactory} 接口对象的构造或方法）
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ModContainer {

    /**
     * RegistryName
     */
    String value() default "";

    /**
     * 同时注册的 Screen
     * 该 Screen 应实现 IHasContainer 接口，通常为 ContainerScreen
     * 参数
     *  net.minecraft.inventory.container.Container,
     *  net.minecraft.entity.player.PlayerInventory,
     *  net.minecraft.util.text.ITextComponent
     * 返回值
     *  net.minecraft.client.gui.screen.Screen
     * @see net.minecraft.client.gui.screen.Screen
     * @see net.minecraft.client.gui.IHasContainer
     * @see net.minecraft.client.gui.screen.inventory.ContainerScreen
     */
    Method2 screen() default @Method2;

    /**
     * 单独注册 Screen
     * 该 Screen 应实现 IHasContainer 接口，通常为 ContainerScreen
     * 该注解应当应用到 Screen 类的相关内容中
     *  若应用到 ContainerScreen 类，则该类应当有一个可被使用的构造函数
     *  若应用到一个方法上，则该方法标志应当为一个可被使用的方法
     * 有关可被使用
     *  可被使用 是指 接受
     *      (Container container, PlayerInventory inventory, ITextComponent text)
     *  类型的参数的 Screen 子类的构造函数或返回 Screen 对象的方法
     *  （即 可直接转化为 {@link net.minecraft.client.gui.ScreenManager.IScreenFactory} 接口对象的构造或方法）
     * 注意
     *    该注解不会同时注册 ContainerType。正常情况下不应当使用此注解
     *    该方法仅仅作为 {@link net.minecraft.client.gui.ScreenManager#registerFactory(ContainerType, ScreenManager.IScreenFactory)}
     *  的一个快捷方式使用。
     * @see net.minecraft.client.gui.screen.Screen
     * @see net.minecraft.client.gui.IHasContainer
     * @see net.minecraft.client.gui.screen.inventory.ContainerScreen
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface Screen {
        /**
         * 获取对应的 ContainerType 对象
         */
        Getter value();
    }
}
