package com.elementtimes.elementcore.api.annotation.part;

import com.elementtimes.elementcore.api.annotation.part.Getter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 代表一个挖掘等级
 * @author luqin2007
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ToolType {
    /**
     * 挖掘工具，返回一个 {@link net.minecraftforge.common.ToolType} 对象
     * @return 挖掘工具
     */
    Getter tool() default @Getter;

    /**
     * 挖掘等级
     * @return 挖掘等级
     */
    int level() default 0;
}
