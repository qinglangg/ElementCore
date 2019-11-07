package com.elementtimes.elementcore.api.template.capability.fluid;

import com.elementtimes.elementcore.api.utils.FluidUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/**
 * 自定义实现的 IFluidHandler
 * 基于 FluidHandlerConcatenate，可以存储不同种类的流体
 * 实现了 NBT 序列化/反序列化
 * 实现了输入输出的过滤功能
 *
 * @author luqin2007
 */
@SuppressWarnings("WeakerAccess")
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
            handlers[i] = new FluidTank(capacity) {
                @Override
                public FluidStack drain(int maxDrain, boolean doDrain) {
                    if (fluid == null || !drankCheck.test(i, new FluidStack(fluid, maxDrain))) {
                        return null;
                    }
                    return super.drain(maxDrain, doDrain);
                }

                @Override
                public FluidStack drain(FluidStack resource, boolean doDrain) {
                    if (resource == null || !drankCheck.test(i, resource)) {
                        return null;
                    }
                    return super.drain(resource, doDrain);
                }

                @Override
                public int fill(FluidStack resource, boolean doFill) {
                    if (resource == null || !fillCheck.test(i, resource)) {
                        return 0;
                    }
                    return super.fill(resource, doFill);
                }
            };
            last = capacity;
        }
        return handlers;
    }

    public TankHandler(BiPredicate<Integer, FluidStack> fillCheck, BiPredicate<Integer, FluidStack> drankCheck, int size, int... capacities) {
        super(build(fillCheck, drankCheck, size, capacities));
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT fluids = new ListNBT();
        for (IFluidHandler handler : subHandlers) {
            CompoundNBT nbt = new CompoundNBT();
            if (handler != null) {
                FluidTank tank = (FluidTank) handler;
                nbt.put("_tank_", tank.writeToNBT(new CompoundNBT()));
                nbt.putInt("_capacity_", tank.getCapacity());
            }
            fluids.add(nbt);
        }
        tag.put("_fluids_", fluids);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("_fluids_")) {
            ListNBT list = nbt.getList("_fluids_", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < Math.min(subHandlers.length, list.size()); i++) {
                CompoundNBT nbtFluid = (CompoundNBT) list.get(i);
                FluidTank tank = new FluidTank(nbtFluid.getInt("_capacity_"));
                tank.readFromNBT(nbtFluid.getCompound("_tank_"));
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
    public int drain(int slot, FluidStack resource, boolean doDrain) {
        return subHandlers[slot].drain(resource, doDrain).amount;
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
    public FluidStack getFluid(int slot) {
        FluidStack fluid = ((IFluidTank) subHandlers[slot]).getFluid();
        return fluid == null ? null : fluid.copy();
    }

    @Override
    public int getCapacity(int slot) {
        return ((IFluidTank) subHandlers[slot]).getCapacity();
    }

    @Override
    public void setSlot(int slot, Fluid fluid, int amount) {
        ((FluidTank) subHandlers[slot]).setFluid(new FluidStack(fluid, amount));
    }

    @Override
    public void setSlot(int slot, int amount) {
        FluidStack fluid = ((FluidTank) subHandlers[slot]).getFluid();
        if (fluid != null) {
            ((FluidTank) subHandlers[slot]).setFluid(new FluidStack(fluid, amount));
        }
    }
}
