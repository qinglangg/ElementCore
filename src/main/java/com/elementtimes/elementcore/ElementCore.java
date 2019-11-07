package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.ECModElements;
import net.minecraftforge.fml.common.Mod;

/**
 * 元素核心
 * @author luqin2007
 */
@SuppressWarnings({"unused"})
@Mod(ElementCore.MODID)
public class ElementCore {
    public static ElementCore INSTANCE = null;

    static final String MODID = "elementcore";

    public static ECModElements.Builder builder() {
        return ECModElements.builder();
    }

    public ECModElements container = builder().build();

    public ElementCore() {
        INSTANCE = this;
    }
}
