package com.elementtimes.elementcore;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
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
        ElementCore.instance().container.elements().getClientElements().fmlEventRegister.onPreInit(event);
    }

    @Override
    public void onServerStart(FMLServerStartingEvent event) { }
}
