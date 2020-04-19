package com.elementtimes.elementcore.api.common.net.handler;

import com.elementtimes.elementcore.api.common.net.GuiFluidNetwork;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashMap;
import java.util.Map;

public class GuiFluidHandler implements IMessageHandler<GuiFluidNetwork, IMessage> {

    @Override
    public IMessage onMessage(GuiFluidNetwork message, MessageContext ctx) {
        synchronized (this) {
            if (message.isValid) {
                Map<SideHandlerType, Int2ObjectMap<ImmutablePair<FluidStack, Integer>>> fluids = new HashMap<>();
                message.fluids.keySet().forEach(type -> {
                    Int2ObjectMap<FluidStack> rFluids = message.fluids.get(type);
                    Int2IntMap rCapabilities = message.capabilities.get(type);
                    fluids.put(type, new Int2ObjectArrayMap<>(rFluids.size()));
                    rFluids.keySet().forEach(slot -> {
                        FluidStack fluidStack = rFluids.get(slot);
                        int capability = rCapabilities.get(slot);
                        fluids.get(type).put(slot, ImmutablePair.of(fluidStack, capability));
                    });
                });
                com.elementtimes.elementcore.api.template.gui.client.GuiDataFromServer.FLUIDS.put(message.guiType, fluids);
            }
        }
        return null;
    }
}
