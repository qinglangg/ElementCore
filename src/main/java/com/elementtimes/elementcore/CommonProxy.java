package com.elementtimes.elementcore;

import com.elementtimes.elementcore.ElementCore;
import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * @author luqin2007
 */
public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        ElementCore.instance().container = ECModElements.builder().disableDebugMessage().build(event);
    }

    public void init(FMLInitializationEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onInit(event);
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onPostInit(event);
        }
    }

    public void onServerStart(FMLServerStartingEvent event) {
        for (ECModContainer mod : ECModContainer.MODS.values()) {
            mod.elements.fmlEventRegister.onServerStart(event);
        }
    }
}
