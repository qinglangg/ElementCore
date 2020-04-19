package com.elementtimes.elementcore.api.annotation.part;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 药水效果
 * 用于创建一个 PotionType
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface PotionType {
    String baseName() default "";
    String registryName() default "";
    PotionEffect[] effects() default {};
}
