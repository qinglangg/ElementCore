package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.ECModContainer;
import com.elementtimes.elementcore.api.ECModElements;
import net.minecraftforge.fml.common.Mod;

/**
 * 元素核心
 * TODO
 *  0 Command: 命令修复
 *  1 Tag: 代码注册
 *  6 RecipeLoader: 注册配方
 * @author luqin2007
 */
@Mod(ElementCore.MODID)
public class ElementCore {
    public static ElementCore INSTANCE = null;
    public static ECModContainer CONTAINER;

    static final String MODID = "elementcore";

    public static ECModElements.Builder builder() {
        return ECModElements.builder();
    }


    public ElementCore() {
        INSTANCE = this;
        CONTAINER = builder()
                .useSimpleNetwork()
                .build();
    }
}
