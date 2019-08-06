package com.elementtimes.elementcore.annotation;

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
