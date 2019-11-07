package com.elementtimes.elementcore.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 工作台合成
 * 注解 IRecipe 类/对象上
 * 类型为 IRecipe/Supplier<IRecipe> 或 Object[]，或生成他们的方法。
 * 若为数组或集合，且该数组或集合第一个元素为 IRecipe/Supplier<IRecipe>，则只会解析 IRecipe/Supplier<IRecipe>
 *     否则，会被当作一个合成表解析，第一个元素为返回物品，其余依次为合成槽位内的物品
 * 数组/列表值使用 CraftingHelper.getIngredient 解析
 * @deprecated json 加载。暂时没有写代码加载
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Deprecated
@SuppressWarnings("unused")
public @interface ModRecipe {

    /**
     * 合成表名
     * 留空则使用 Field 变量名/Class 名
     * @return 合成表名
     */
    String value() default "";

    /**
     * @return 是否为有序合成
     */
    boolean shaped() default true;

    /**
     * @return 是否为矿辞合成
     */
    boolean ore() default true;

    /**
     * @return 有序合成的宽
     */
    int width() default 3;

    /**
     * @return 有序合成的高
     */
    int height() default 3;
}
