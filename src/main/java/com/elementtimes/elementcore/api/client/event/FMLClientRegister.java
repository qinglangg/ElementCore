package com.elementtimes.elementcore.api.client.event;

import com.elementtimes.elementcore.api.common.ECModContainer;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.ECUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FMLClientRegister {

    private ECModContainer mContainer;

    public FMLClientRegister(ECModContainer container) {
        mContainer = container;
    }

    public ECModElements elements() {
        return mContainer.elements();
    }

    public void onPreInit(FMLPreInitializationEvent event) {
        ECUtils.common.runWithModActive(mContainer.mod, () -> {
            elements().getClientElements().commands.forEach(ClientCommandHandler.instance::registerCommand);
        }, event);
    }
}
