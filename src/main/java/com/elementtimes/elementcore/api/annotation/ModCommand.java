package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令系统
 * 注册到 ICommand 的实现（通常继承 CommandBase 或 CommandTreeBase）
 * 若注册到类，则需要有一个无参构造
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface ModCommand {
    /**
     * @return 是否为客户端命令
     */
    boolean client() default false;
}
