package com.elementtimes.elementcore;

import com.elementtimes.elementcore.api.common.ECModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author luqin2007
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ECModContainer.MODS.values().forEach(container -> {
            container.elements().getClientElements().fmlEventRegister.onPreInit(event);
        });
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ECModContainer.MODS.values().forEach(container -> {
            container.elements().getClientElements().fmlEventRegister.onInit(event);
        });
    }
}
