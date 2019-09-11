package com.elementtimes.elementcore.api.template.capability.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface ITankHandler extends IFluidHandler, INBTSerializable<NBTTagCompound> {

    @Override
    @Deprecated
    int fill(FluidStack resource, boolean doFill);

    @Override
    @Deprecated
    FluidStack drain(FluidStack resource, boolean doDrain);

    int fill(int slot, FluidStack resource, boolean doFill);

    int fillIgnoreCheck(int slot, FluidStack resource, boolean doFill);

    FluidStack drain(int slot, FluidStack resource, boolean doDrain);

    FluidStack drainIgnoreCheck(int slot, FluidStack resource, boolean doDrain);

    FluidStack drain(int slot, int maxDrain, boolean doDrain);

    FluidStack drainIgnoreCheck(int slot, int maxDrain, boolean doDrain);
}
