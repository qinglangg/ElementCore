package com.elementtimes.elementcore.api.template.capability;

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
     * 同时可以输入/输出
     */
    IN_OUT(2),

    /**
     * 无
     */
    NONE(3);

    public final int id;

    SideHandlerType(int id) {
        this.id = id;
    }

    public static SideHandlerType get(int id) {
        switch (id) {
            case 0: return INPUT;
            case 1: return OUTPUT;
            case 2: return IN_OUT;
            default: return NONE;
        }
    }
}
