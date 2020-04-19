package com.elementtimes.elementcore.api.common.net.handler;

import com.elementtimes.elementcore.api.common.net.TESRRenderNetwork;
import com.elementtimes.elementcore.api.template.tileentity.interfaces.ITileTESR;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TESRRenderHandler implements IMessageHandler<TESRRenderNetwork, IMessage> {

    @Override
    public IMessage onMessage(TESRRenderNetwork message, MessageContext ctx) {
        synchronized (this) {
            if (message.isValid && net.minecraftforge.fml.client.FMLClientHandler.instance().getWorldClient().provider.getDimension() == message.dim) {
                TileEntity te = net.minecraftforge.fml.client.FMLClientHandler.instance().getWorldClient().getTileEntity(message.pos);
                if (te instanceof ITileTESR) {
                    ((ITileTESR) te).receiveRenderMessage(message.nbt);
                }
            }
        }
        return null;
    }
}
