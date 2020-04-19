package com.elementtimes.elementcore.api.book.screen;

/**
 * 绘制阶段
 * @author luqin2007
 */
public enum DrawStage {
    /**
     * 构造中
     */
    CONSTRUCTOR,

    /**
     * init 方法中
     */
    INIT,

    /**
     * drawBackground 中
     */
    BACKGROUND,

    /**
     * drawForeground 中
     */
    FOREGROUND,

    /**
     * Container 类的构造函数中
     */
    CONTAINER
}
