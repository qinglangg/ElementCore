package com.elementtimes.elementcore.common.network;

import com.elementtimes.elementcore.api.annotation.ModNetwork;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.capability.fluid.FluidTankInfo;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@ModNetwork
public class GuiDataNetwork {

    public static Int2ObjectMap<GuiDataNetwork> DATA = new Int2ObjectArrayMap<>(4);

    public int guiType;

    // fluids
    public Int2ObjectMap<FluidTankInfo> fluidInputs;
    public Int2ObjectMap<FluidTankInfo> fluidOutputs;
    public Int2ObjectMap<FluidTankInfo> fluidOthers;

    // energy
    public int capacity, stored;

    // process
    public int process, total;

    public static void encoder(GuiDataNetwork data, PacketBuffer buffer) {
        buffer.writeInt(data.guiType);
        buffer.writeInt(data.capacity);
        buffer.writeInt(data.stored);
        buffer.writeInt(data.process);
        buffer.writeInt(data.total);
        buffer.writeInt(data.fluidInputs.size());
        buffer.writeInt(data.fluidOutputs.size());
        buffer.writeInt(data.fluidOthers.size());
        for (Int2ObjectMap.Entry<FluidTankInfo> info : data.fluidInputs.int2ObjectEntrySet()) {
            buffer.writeInt(info.getIntKey());
            buffer.writeInt(info.getValue().capacity);
            buffer.writeInt(info.getValue().amount);
            buffer.writeString(info.getValue().fluid.getRegistryName().toString());
        }
        for (Int2ObjectMap.Entry<FluidTankInfo> info : data.fluidOutputs.int2ObjectEntrySet()) {
            buffer.writeInt(info.getIntKey());
            buffer.writeInt(info.getValue().capacity);
            buffer.writeInt(info.getValue().amount);
            buffer.writeString(info.getValue().fluid.getRegistryName().toString());
        }
        for (Int2ObjectMap.Entry<FluidTankInfo> info : data.fluidOthers.int2ObjectEntrySet()) {
            buffer.writeInt(info.getIntKey());
            buffer.writeInt(info.getValue().capacity);
            buffer.writeInt(info.getValue().amount);
            buffer.writeString(info.getValue().fluid.getRegistryName().toString());
        }
    }

    public static GuiDataNetwork decoder(PacketBuffer buffer) {
        GuiDataNetwork network = new GuiDataNetwork();
        network.guiType = buffer.readInt();
        network.capacity = buffer.readInt();
        network.stored = buffer.readInt();
        network.process = buffer.readInt();
        network.total = buffer.readInt();
        int inputSize = buffer.readInt();
        int outputSize = buffer.readInt();
        int otherSize = buffer.readInt();
        network.fluidInputs = new Int2ObjectArrayMap<>(inputSize);
        for (int i = 0; i < inputSize; i++) {
            int key = buffer.readInt();
            int capacity = buffer.readInt();
            int amount = buffer.readInt();
            Fluid fluid = ECUtils.fluid.getFluid(buffer.readString());
            network.fluidInputs.put(key, new FluidTankInfo(fluid, amount, capacity));
        }
        network.fluidOutputs = new Int2ObjectArrayMap<>(outputSize);
        for (int i = 0; i < outputSize; i++) {
            int key = buffer.readInt();
            int capacity = buffer.readInt();
            int amount = buffer.readInt();
            Fluid fluid = ECUtils.fluid.getFluid(buffer.readString());
            network.fluidOutputs.put(key, new FluidTankInfo(fluid, amount, capacity));
        }
        network.fluidOthers = new Int2ObjectArrayMap<>(otherSize);
        for (int i = 0; i < otherSize; i++) {
            int key = buffer.readInt();
            int capacity = buffer.readInt();
            int amount = buffer.readInt();
            Fluid fluid = ECUtils.fluid.getFluid(buffer.readString());
            network.fluidOthers.put(key, new FluidTankInfo(fluid, amount, capacity));
        }
        return network;
    }

    public static void consumer(GuiDataNetwork message, Supplier<NetworkEvent.Context> context) {
        synchronized (GuiDataNetwork.class) {
            GuiDataNetwork.DATA.put(message.guiType, message);
        }
    }
}
