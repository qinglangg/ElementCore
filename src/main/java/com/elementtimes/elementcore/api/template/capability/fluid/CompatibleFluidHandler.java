package com.elementtimes.elementcore.api.template.capability.fluid;

import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * 对 Minecraft Fluid 的 IFluidHandler 的兼容层
 * @author luqin2007
 */
public class CompatibleFluidHandler implements ITankHandler {

    private IFluidHandler handler;

    public CompatibleFluidHandler(IFluidHandler handler) {
        this.handler = handler;
    }

    @Override
    public int fill(int slot, FluidStack resource, boolean doFill) {
        return handler.fill(slot, ECUtils.fluid.convert(resource), doFill);
    }

    @Override
    public int fillIgnoreCheck(int slot, FluidStack resource, boolean doFill) {
        return handler.fillNotFilter(slot, ECUtils.fluid.convert(resource.getFluid()), resource.amount, doFill);
    }

    @Override
    public int drain(int slot, FluidStack resource, boolean doDrain) {
        return handler.drain(slot, ECUtils.fluid.convert(resource), doDrain).getAmount();
    }

    @Override
    public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
        return ECUtils.fluid.convert(handler.drain(slot, maxDrain, doDrain));
    }

    @Override
    public FluidStack drainIgnoreCheck(int slot, FluidStack resource, boolean doDrain) {
        return ECUtils.fluid.convert(handler.drainNotFilter(slot, ECUtils.fluid.convert(resource).getFluid(), resource.amount, doDrain));
    }

    @Override
    public FluidStack drainIgnoreCheck(int slot, int maxDrain, boolean doDrain) {
        return ECUtils.fluid.convert(handler.drainNotFilter(slot, handler.getFluid(slot), maxDrain, doDrain));
    }

    @Override
    public int size() {
        return handler.getSize();
    }

    @Override
    public FluidStack getFluid(int slot) {
        return ECUtils.fluid.convert(handler.getFluidStack(slot));
    }

    @Override
    public int getCapacity(int slot) {
        return handler.getCapacity(slot);
    }

    @Override
    public void setSlot(int slot, Fluid fluid, int amount) {
        handler.setFluid(slot, ECUtils.fluid.convert(fluid), amount);
    }

    @Override
    public void setSlot(int slot, int amount) {
        handler.setFluid(slot, handler.getFluid(slot), amount);
    }

    @Override
    public CompoundNBT serializeNBT() {
        return handler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        handler.write(nbt);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return Arrays.stream(handler.getTankInfos())
                .map(info ->
                        new FluidTankProperties(new FluidStack(ECUtils.fluid.convert(info.fluid), info.amount), info.capacity))
                .toArray(IFluidTankProperties[]::new);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return handler.fill(ECUtils.fluid.convert(resource), doFill);
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return ECUtils.fluid.convert(handler.drain(ECUtils.fluid.convert(resource), doDrain));
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return ECUtils.fluid.convert(handler.drain(maxDrain, doDrain));
    }
}
