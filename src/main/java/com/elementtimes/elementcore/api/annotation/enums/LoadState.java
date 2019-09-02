package com.elementtimes.elementcore.api.annotation.enums;

/**
 * 加载时机
 * @author luqin2007
 */
public enum LoadState {
    /**
     * FMLPreInitializationEvent
     */
    PreInit,
    /**
     * FMLInitializationEvent
     */
    Init,
    /**
     * FMLPostInitializationEvent
     */
    PostInit
}
