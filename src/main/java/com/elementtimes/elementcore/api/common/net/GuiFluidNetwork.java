package com.elementtimes.elementcore.api.common.net;

import com.elementtimes.elementcore.api.annotation.ModSimpleNetwork;
import com.elementtimes.elementcore.api.annotation.part.Getter;
import com.elementtimes.elementcore.api.common.net.handler.GuiFluidHandler;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import com.elementtimes.elementcore.api.template.tileentity.lifecycle.HandlerInfoMachineLifecycle;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于流体参与的机器的通信
 * @author luqin2007
 */
@ModSimpleNetwork(value = @Getter(GuiFluidHandler.class), side = Side.CLIENT)
public class GuiFluidNetwork implements IMessage {

    public Map<SideHandlerType, Int2ObjectMap<FluidStack>> fluids = new HashMap<>();
    public Map<SideHandlerType, Int2IntMap> capabilities = new HashMap<>();
    public int guiType;
    public boolean isValid = false;

    public GuiFluidNetwork() { }

    public void put(int gui, HandlerInfoMachineLifecycle.FluidInfo infos) {
        guiType = gui;
        for (Map.Entry<SideHandlerType, IFluidHandler> entry : infos.fluids.entrySet()) {
            IFluidTankProperties[] properties = entry.getValue().getTankProperties();
            Int2ObjectMap<FluidStack> rFluids = new Int2ObjectArrayMap<>(properties.length);
            Int2IntMap rCapabilities = new Int2IntArrayMap(properties.length);
            for (int i = 0; i < properties.length; i++) {
                rFluids.put(i, properties[i].getContents());
                rCapabilities.put(i, properties[i].getCapacity());
            }
            fluids.put(entry.getKey(), rFluids);
            capabilities.put(entry.getKey(), rCapabilities);
        }
    }

    /**
     * int typeCount
     * [
     *  int gui
     *  String type
     *  int count
     *  [
     *      NBT fluid
     *      int capability
     *  ]
     * ]
     * @param buf bug
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            guiType = buf.readInt();
            int typeCount = buf.readInt();
            for (int t = 0; t < typeCount; t++) {
                SideHandlerType type = SideHandlerType.get(buf.readInt());
                int count = buf.readInt();
                Int2ObjectMap<FluidStack> rFluids = new Int2ObjectArrayMap<>(count);
                Int2IntMap rCapabilities = new Int2IntArrayMap(count);
                for (int i = 0; i < count; i++) {
                    int slot = buf.readInt();
                    NBTTagCompound compound = ByteBufUtils.readTag(buf);
                    if (compound == null || compound.hasNoTags()) {
                        rFluids.put(slot, null);
                    } else {
                        rFluids.put(slot, FluidStack.loadFluidStackFromNBT(compound));
                    }
                    rCapabilities.put(slot, buf.readInt());
                }
                fluids.put(type, rFluids);
                capabilities.put(type, rCapabilities);
            }
            isValid = true;
        } catch (Exception ignore) {}
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(guiType);
        buf.writeInt(fluids.size());
        fluids.keySet().forEach(type -> {
            buf.writeInt(type.id);
            Int2ObjectMap<FluidStack> rFluids = fluids.get(type);
            Int2IntMap rCapabilities = capabilities.get(type);
            buf.writeInt(rFluids.size());
            rFluids.keySet().forEach(slot -> {
                buf.writeInt(slot);
                FluidStack fluid = rFluids.get(slot);
                if (fluid == null) {
                    ByteBufUtils.writeTag(buf, new NBTTagCompound());
                } else {
                    ByteBufUtils.writeTag(buf, fluid.writeToNBT(new NBTTagCompound()));
                }
                buf.writeInt(rCapabilities.get(slot));
            });
        });
    }
}
