package com.elementtimes.elementcore.api.common.net.handler;

import com.elementtimes.elementcore.api.common.net.GuiEnergyNetwork;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GuiEnergyHandler implements IMessageHandler<GuiEnergyNetwork, IMessage> {

    @Override
    public IMessage onMessage(GuiEnergyNetwork message, MessageContext ctx) {
        synchronized (this) {
            if (message.isValid) {
                com.elementtimes.elementcore.api.template.gui.client.GuiDataFromServer.ENERGIES.put(message.guiType,
                        new HandlerInfoMachineLifecycle.EnergyInfo(message.capacity, message.energy));
            }
        }
        return null;
    }
}