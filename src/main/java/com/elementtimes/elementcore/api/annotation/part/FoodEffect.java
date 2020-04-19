package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.annotation.part.EffectInstance;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 食物的药水效果
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface FoodEffect {
    /**
     * 药水效果
     * @return 药水效果
     */
    EffectInstance instance();

    /**
     * 获取概率
     * @return 获取概率
     */
    float probability() default 1f;
}
