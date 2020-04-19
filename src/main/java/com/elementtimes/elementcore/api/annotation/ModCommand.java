package com.elementtimes.elementcore.api.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令
 * 当该注解应用到一个类或成员变量时，直接注册其命令
 *  此时，被注册的类或对象类型应为
 *      {@link com.mojang.brigadier.tree.CommandNode<net.minecraft.command.CommandSource>}
 *  或
 *      {@link com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.command.CommandSource>}
 * 一般来说，通过 Commands.literal 方法创建
 * @see net.minecraft.command.Commands#literal(String)
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ModCommand { }
