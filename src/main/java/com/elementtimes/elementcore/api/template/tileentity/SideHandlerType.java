package com.elementtimes.elementcore.api.template.tileentity;

/**
 * @author luqin2007
 */
public enum SideHandlerType {

    /**
     * 输入槽
     */
    INPUT(0),

    /**
     * 输出槽
     */
    OUTPUT(1),

    /**
     * 暂未使用
     * 同时可以输入/输出
     */
    IN_OUT(2),

    /**
     * 不可输入输出
     * 但可以读出存储物品
     * 暂时仅电量存储实现
     */
    READONLY(3),

    /**
     * 未知，不知道干啥
     */
    NONE(4),

    /**
     * 访问所有
     */
    ALL(-1);

    public final int id;

    SideHandlerType(int id) {
        this.id = id;
    }

    public static SideHandlerType get(int id) {
        switch (id) {
            case 0: return INPUT;
            case 1: return OUTPUT;
            case 2: return IN_OUT;
            case 3: return READONLY;
            case 4: return NONE;
            default: return ALL;
        }
    }
}
