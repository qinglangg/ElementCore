package com.elementtimes.elementcore.api.common.net;

import com.elementtimes.elementcore.api.annotation.ModSimpleNetwork;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.common.net.handler.GuiEnergyHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

@ModSimpleNetwork(value = @Getter(GuiEnergyHandler.class), side = Side.CLIENT)
public class GuiEnergyNetwork implements IMessage {

    public int capacity;
    public int energy;
    public int guiType;
    public boolean isValid = false;

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
}
