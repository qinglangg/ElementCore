package com.elementtimes.elementcore.common.network;

import com.elementtimes.elementcore.api.annotation.ModNetwork;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

@ModNetwork(handlerClass = "com.elementtimes.elementcore.common.network.GuiEnergyNetwork$Handler", side = Side.CLIENT)
public class GuiEnergyNetwork implements IMessage {

    int capacity;
    int energy;
    int guiType;
    private boolean isValid = false;

    public GuiEnergyNetwork(int gui, int capacity, int energy) {
        this.guiType = gui;
        this.capacity = capacity;
        this.energy = energy;
    }

    public GuiEnergyNetwork() {
        this(0, 0, 0);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            guiType = buf.readInt();
            capacity = buf.readInt();
            energy = buf.readInt();
            isValid = true;
        } catch (RuntimeException ignore) {}
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(guiType);
        buf.writeInt(capacity);
        buf.writeInt(energy);
    }

    public static class Handler implements IMessageHandler<GuiEnergyNetwork, IMessage> {

        public Handler() {}

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
}
