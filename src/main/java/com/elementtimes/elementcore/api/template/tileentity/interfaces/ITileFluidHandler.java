package com.elementtimes.elementcore.api.template.tileentity.interfaces;

import com.elementtimes.elementcore.api.template.capability.fluid.ITankHandler;
import com.elementtimes.elementcore.api.template.tileentity.SideHandlerType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 流体存储有关的接口
 * @author luqin2007
 */
public interface ITileFluidHandler extends ICapabilityProvider, INBTSerializable<NBTTagCompound> {

    String NBT_FLUID = "_fluids_";
    String NBT_FLUID_INPUT = "_inputs_";
    String NBT_FLUID_OUTPUT = "_outputs_";

    ITankHandler getTanks(SideHandlerType type);

    default SideHandlerType getTankType(EnumFacing facing) {
        return SideHandlerType.ALL;
    }

    default void setTanks(SideHandlerType type, ITankHandler handler) {};

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability.cast((T) getTanks(getTankType(facing)));
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey(NBT_FLUID)) {
            NBTTagCompound fluids = nbt.getCompoundTag(NBT_FLUID);
            if (fluids.hasKey(NBT_FLUID_INPUT)) {
                final ITankHandler input = getTanks(SideHandlerType.INPUT);
                final NBTTagCompound compoundTag = fluids.getCompoundTag(NBT_FLUID_INPUT);
                if (input != null) {
                    input.deserializeNBT(compoundTag);
                }
            }
            if (fluids.hasKey(NBT_FLUID_OUTPUT)) {
                final ITankHandler output = getTanks(SideHandlerType.OUTPUT);
                final NBTTagCompound compoundTag = fluids.getCompoundTag(NBT_FLUID_OUTPUT);
                if (output != null) {
                    output.deserializeNBT(compoundTag);
                }
            }
        }
    }

    @Override
    default NBTTagCompound serializeNBT() {
        return writeToNBT(new NBTTagCompound());
    }

    default NBTTagCompound writeToNBT(NBTTagCompound nbtTagCompound)  {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag(NBT_FLUID_INPUT, getTanks(SideHandlerType.INPUT).serializeNBT());
        nbt.setTag(NBT_FLUID_OUTPUT, getTanks(SideHandlerType.OUTPUT).serializeNBT());
        nbtTagCompound.setTag(NBT_FLUID, nbt);
        return nbtTagCompound;
    }

    default boolean isFillValid(int slot, FluidStack fluidStack) { return false; }
}
