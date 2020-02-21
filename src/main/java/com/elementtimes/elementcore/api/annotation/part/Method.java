package com.elementtimes.elementcore.api.annotation.part;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表一个方法。只限定方法所在类和方法名，其他需求详见使用到的地方的注释
 * 也负责一个类的实例化
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Method {

    /**
     * 指向的类
     * 当该类为 Method.class 即指向该注解本身时，则返回 null
     *  通常 这种情况是为了 default 占位
     * @return 类
     */
    Class<?> value() default Method.class;

    /**
     * 方法名
     * 当该值为 <init> 时，表示使用构造函数初始化类实例
     * @return 方法名
     */
    String name() default "<init>";

    /**
     * 该方法是否被 static 修饰。
     * 如果该值为 false，则 containerObj 值应当指向一个有效的实例
     * @return 该方法是否为静态方法
     */
    boolean isStatic() default true;

    /**
     * 当 isStatic 值为 false 时，将通过该属性获取对应实例
     * @return 非静态方法的容器
     */
    Getter holder() default @Getter;
}
