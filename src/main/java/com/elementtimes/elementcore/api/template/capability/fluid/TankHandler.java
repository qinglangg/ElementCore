package com.elementtimes.elementcore.api.template.capability.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

import java.util.function.BiPredicate;

/**
 * 自定义实现的 IFluidHandler
 * 基于 FluidHandlerConcatenate，可以存储不同种类的流体
 * 实现了 NBT 序列化/反序列化
 * 实现了输入输出的过滤功能
 *
 * @author luqin2007
 */
public class TankHandler extends FluidHandlerConcatenate implements ITankHandler {

    public static final TankHandler EMPTY = new TankHandler(TankHandler.FALSE, TankHandler.FALSE, 0);
    public static final BiPredicate<Integer, FluidStack> TRUE = (i, fluid) -> true;
    public static final BiPredicate<Integer, FluidStack> FALSE = (i, fluid) -> false;

    private static IFluidHandler[] build(BiPredicate<Integer, FluidStack> fillCheck, BiPredicate<Integer, FluidStack> drankCheck, int size, int... capacities) {
        IFluidHandler[] handlers = new IFluidHandler[size];
        int last = -1;
        for (int i1 = 0; i1 < size; i1++) {
            final int i = i1;
            int capacity;
            if (capacities.length > i) {
                capacity = capacities[i];
            } else if (last > 0) {
                capacity = last;
            } else {
                capacity = 1000;
            }
            handlers[i] = new Tank(fillCheck, drankCheck, capacity, i);
            last = capacity;
        }
        return handlers;
    }

    private BiPredicate<Integer, FluidStack> fillCheck, drankCheck;

    public TankHandler(BiPredicate<Integer, FluidStack> fillCheck, BiPredicate<Integer, FluidStack> drankCheck, int size, int... capacities) {
        super(build(fillCheck, drankCheck, size, capacities));
        this.fillCheck = fillCheck;
        this.drankCheck = drankCheck;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList fluids = new NBTTagList();
        for (IFluidHandler handler : subHandlers) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (handler != null) {
                FluidTank tank = (FluidTank) handler;
                nbt.setTag("_tank_", tank.writeToNBT(new NBTTagCompound()));
                nbt.setInteger("_capacity_", tank.getCapacity());
            }
            fluids.appendTag(nbt);
        }
        tag.setTag("_fluids_", fluids);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("_fluids_")) {
            NBTTagList list = (NBTTagList) nbt.getTag("_fluids_");
            for (int i = 0; i < Math.min(subHandlers.length, list.tagCount()); i++) {
                NBTTagCompound nbtFluid = (NBTTagCompound) list.get(i);
                FluidTank tank = new Tank(fillCheck, drankCheck, nbtFluid.getInteger("_capacity_"), i);
                tank.readFromNBT(nbtFluid.getCompoundTag("_tank_"));
                subHandlers[i] = tank;
            }
        }
    }

    @Override
    public int fill(int slot, FluidStack resource, boolean doFill) {
        return subHandlers[slot].fill(resource, doFill);
    }

    @Override
    public int fillIgnoreCheck(int slot, FluidStack resource, boolean doFill) {
        return ((FluidTank) subHandlers[slot]).fillInternal(resource, doFill);
    }

    @Override
    public FluidStack drain(int slot, FluidStack resource, boolean doDrain) {
        return subHandlers[slot].drain(resource, doDrain);
    }

    @Override
    public FluidStack drainIgnoreCheck(int slot, FluidStack resource, boolean doDrain) {
       return ((FluidTank) subHandlers[slot]).drainInternal(resource, doDrain);
    }

    @Override
    public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
        return subHandlers[slot].drain(maxDrain, doDrain);
    }

    @Override
    public FluidStack drainIgnoreCheck(int slot, int maxDrain, boolean doDrain) {
        return ((FluidTank) subHandlers[slot]).drainInternal(maxDrain, doDrain);
    }

    @Override
    public int size() {
        return subHandlers.length;
    }

    @Override
    public FluidStack getFluid(int slot, boolean copy) {
        FluidStack fluid = ((IFluidTank) subHandlers[slot]).getFluid();
        if (fluid == null || !copy) {
            return fluid;
        } else {
            return fluid.copy();
        }
    }

    @Override
    public int getCapacity(int slot) {
        return ((IFluidTank) subHandlers[slot]).getCapacity();
    }

    static class Tank extends FluidTank {

        public int slot;
        private BiPredicate<Integer, FluidStack> fillCheck, drankCheck;

        public Tank(BiPredicate<Integer, FluidStack> fillCheck, BiPredicate<Integer, FluidStack> drankCheck, int capability, int slot) {
            super(capability);
            this.slot = slot;
            this.fillCheck = fillCheck;
            this.drankCheck = drankCheck;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (fluid == null || !drankCheck.test(slot, new FluidStack(fluid, maxDrain))) {
                return null;
            }
            return super.drain(maxDrain, doDrain);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || !drankCheck.test(slot, resource)) {
                return null;
            }
            return super.drain(resource, doDrain);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || !fillCheck.test(slot, resource)) {
                return 0;
            }
            return super.fill(resource, doFill);
        }
    }
}
