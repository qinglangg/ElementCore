package com.elementtimes.elementcore.api.annotation.part;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 药水效果
 * 用于创建一个 PotionEffect
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface PotionEffect {
    int duration() default 0;
    int amplifier() default 0;
    boolean ambient() default false;
    boolean showParticles() default true;
}
