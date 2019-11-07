package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.capability.fluid.CompatibleFluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.CapabilityFluidHandler;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import com.elementtimes.elementcore.api.template.interfaces.INbtReadable;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 流体存储有关的接口
 * @author luqin2007
 */
public interface ITileFluidHandler extends ICapabilityProvider, INbtReadable {

    String NBT_FLUID = "_fluids_";
    String NBT_FLUID_INPUT = "_inputs_";
    String NBT_FLUID_OUTPUT = "_outputs_";

    IFluidHandler getTanks(SideHandlerType type);

    default SideHandlerType getTankType(Direction facing) {
        return SideHandlerType.ALL;
    }

    default void setTanks(SideHandlerType type, IFluidHandler handler) {};

    @Override
    default <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> (T) getTanks(getTankType(facing)));
        }
        if (capability == net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> (T) new CompatibleFluidHandler(getTanks(getTankType(facing))));
        }
        return LazyOptional.empty();
    }

    @Override
    default void read(@Nonnull CompoundNBT nbt) {
        CompoundNBT fluids = nbt.getCompound(NBT_FLUID);
        if (fluids.contains(NBT_FLUID_INPUT)) {
            final IFluidHandler input = getTanks(SideHandlerType.INPUT);
            final CompoundNBT compoundTag = fluids.getCompound(NBT_FLUID_INPUT);
            if (input != null) {
                input.deserializeNBT(compoundTag);
            }
        }
        if (fluids.contains(NBT_FLUID_OUTPUT)) {
            final IFluidHandler output = getTanks(SideHandlerType.OUTPUT);
            final CompoundNBT compoundTag = fluids.getCompound(NBT_FLUID_OUTPUT);
            if (output != null) {
                output.deserializeNBT(compoundTag);
            }
        }
    }

    @Nonnull
    @Override
    default CompoundNBT write(@Nonnull CompoundNBT nbtTagCompound)  {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(NBT_FLUID_INPUT, getTanks(SideHandlerType.INPUT).serializeNBT());
        nbt.put(NBT_FLUID_OUTPUT, getTanks(SideHandlerType.OUTPUT).serializeNBT());
        nbtTagCompound.put(NBT_FLUID, nbt);
        return nbtTagCompound;
    }

    default boolean isFillValid(int slot, FluidStack fluidStack) { return false; }
}
