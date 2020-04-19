package com.elementtimes.elementcore.api.annotation.part;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示一个药水效果
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface EffectInstance {
    /**
     * 一种药水效果，需求一种 {@link net.minecraft.potion.Effect}
     * @return 药水效果
     */
    Getter effect();

    /**
     * 持续时间
     * @return 持续时间
     */
    int duration() default 0;

    /**
     * 效果等级
     * @return 效果等级
     */
    int amplifier() default 0;

    /**
     * 是否为环境效果（不知道干啥的）
     * @return 环境效果
     */
    boolean ambient() default false;

    /**
     * 是否显示粒子效果
     * @return 粒子效果
     */
    boolean showParticles() default true;
}
