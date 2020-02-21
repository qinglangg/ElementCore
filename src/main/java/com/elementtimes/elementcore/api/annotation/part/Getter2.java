package com.elementtimes.elementcore.api.annotation.part;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在没有任何参数的条件下，获取一个实例
 * 也可以负责使用一个类的无参构造实例化
 * 这是专门为了 SideOnly(CLIENT) 设计的，使用类的全类名
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Getter2 {
    /**
     * 指向一个类的全类名。
     * 当该类为 ""，则返回 null
     *  通常 这种情况是为了 default 占位
     * @return 类
     */
    String value() default "";

    /**
     * 通过对应类获取实例的方法
     *  <init>：代表使用无参构造实例化该类
     *  ()xxx：以 () 开头加有效成员名时，代表调用一个无参静态方法
     *  xxx：直接为有效成员名，代表获取一个静态变量
     * @return 变量名
     */
    String name() default "<init>";
}
