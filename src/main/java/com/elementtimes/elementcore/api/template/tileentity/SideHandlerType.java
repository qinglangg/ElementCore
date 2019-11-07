package com.elementtimes.elementcore.api.template.tileentity;

/**
 * @author luqin2007
 */
public enum SideHandlerType {

    /**
     * 输入槽
     */
    INPUT,

    /**
     * 输出槽
     */
    OUTPUT,

    /**
     * 暂未使用
     * 同时可以输入/输出
     */
    IN_OUT,

    /**
     * 不可输入输出
     * 但可以读出存储物品
     * 暂时仅电量存储实现
     */
    READONLY,

    /**
     * 未知，不知道干啥
     */
    NONE,

    /**
     * 访问所有
     */
    ALL
}
