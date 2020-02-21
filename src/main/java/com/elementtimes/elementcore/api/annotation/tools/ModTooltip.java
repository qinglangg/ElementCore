package com.elementtimes.elementcore.api.annotation.tools;

import com.elementtimes.elementcore.api.annotation.part.Method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注册一个 ItemStack 的 Tooltip
 * 若被注解元素为 Item/Block 对象或类，则会筛选仅为对应对象的物品栈
 * 若被注解元素为 Fluid 对象或类，则会筛选所有携带有该类型液体的物品栈
 * 若被注解元素为 Enchantment 对象或类，则会筛选所有带有该附魔的物品栈
 * 若被注解元素为 Entity 类，则会筛选对应该实体的怪物蛋的物品栈
 * 否则，会处理所有 ItemStack
 * @author luqin2007
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface ModTooltip {

    /**
     * 获取物品栈的 Tooltip
     * 参数
     *  ItemStack：要附加的物品栈
     *  List<String>：所有附加在该物品上的 Tooltips，可操作该列表修改 Tooltip
     * 返回值
     *  无
     * @return Tooltip 获取器
     */
    Method value();
}
