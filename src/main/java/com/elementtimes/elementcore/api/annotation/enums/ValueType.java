package com.elementtimes.elementcore.api.annotation.enums;

/**
 * 选择值
 * @author luqin2007
 */
public enum ValueType {
    /**
     * 直接使用某参数值或根据注解其他参数值实例化特定实例
     */
    VALUE(),

    /**
     * 如无特殊说明，使用 method 属性获取对应值，参数为被注解对象
     */
    METHOD,

    /**
     * 如无特殊说明，使用 object 属性获取对应值
     */
    OBJECT,

    /**
     * 无值。若不可为无，使用默认值
     */
    NONE
}
