package com.elementtimes.elementcore.api.template.capability.fluid.vanilla;

import com.elementtimes.elementcore.api.template.capability.fluid.FluidTankInfo;
import com.elementtimes.elementcore.api.template.capability.fluid.vanilla.interfaces.IFluidHandler;
import com.elementtimes.elementcore.api.template.fluid.FluidStack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 类似 Java 的 SubList，不过这个做的是加法，对多个 IFluidHandler 的访问器
 * @author luqin2007
 */
public class TankHandlerVisitor implements IFluidHandler {

    private IFluidHandler[] mHandlers;
    private int[] mTankSlots;
    private Readonly mReadonly;
    private int mSum;

    // 使用 list 兼容多人访问
    private IntArrayList mSelect;
    private IntArrayList mSelectSlot;
    private ArrayList<IFluidHandler> mSelectHandler;

    public TankHandlerVisitor(IFluidHandler... handlers) {
        mHandlers = handlers;
        mTankSlots = Arrays.stream(handlers).mapToInt(IFluidHandler::getSize).toArray();
        mSum = Arrays.stream(mTankSlots).sum();
        mSelect = new IntArrayList(mSum);
        mSelectSlot = new IntArrayList(mSum);
        mSelectHandler = new ArrayList<>(mSum);
        mReadonly = new Readonly();
    }

    private int select(int slot) {
        // check
        for (int i = 0; i < mSelect.size(); i++) {
            if (mSelect.getInt(i) == slot) {
                return i;
            }
        }
        int index = -1;
        synchronized (this) {
            for (int i = 0; i < mSelect.size(); i++) {
                if (mSelect.getInt(i) >= 0) {
                    mSelect.set(i, slot);
                    index = i;
                }
            }
            if (index < 0) {
                index = mSelect.size();
                mSelect.add(slot);
                mSelectSlot.add(0);
                mSelectHandler.add(null);
            }
        }
        // select
        int ptr = 0;
        for (int i = 0; i < mTankSlots.length; i++) {
            final int slots = mTankSlots[i];
            if (slot < ptr + slots) {
                mSelectHandler.set(index, mHandlers[i]);
                mSelectSlot.set(index, slot - ptr);
                return index;
            } else {
                ptr += slots;
            }
        }
        return -1;
    }

    @Override
    public FluidTankInfo[] getTankInfos() {
        return Arrays.stream(mHandlers)
                .map(IFluidHandler::getTankInfos)
                .flatMap(Arrays::stream)
                .toArray(FluidTankInfo[]::new);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (!resource.isEmpty()) {
            FluidStack stack = resource.copy();
            for (IFluidHandler handler : mHandlers) {
                stack.reduce(handler.fill(stack, doFill));
                if (stack.isEmpty()) {
                    return resource.getAmount();
                }
            }
            return resource.getAmount() - stack.getAmount();
        }
        return 0;
    }

    @Override
    public int fill(int slot, FluidStack resource, boolean doFill) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).fill(mSelectSlot.getInt(i), resource, doFill);
        }
        return 0;
    }

    @Override
    public Fluid getFluid(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).getFluid(mSelectSlot.getInt(i));
        }
        return Fluids.EMPTY;
    }

    @Override
    public FluidStack getFluidStack(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).getFluidStack(mSelectSlot.getInt(i));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setFluid(int slot, Fluid fluid, int amount) {
        final int i = select(slot);
        if (i >= 0) {
            setFluid(slot, fluid, amount);
        }
    }

    @Override
    public int getSize() {
        return mSum;
    }

    @Override
    public boolean isFull(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).isFull(mSelectSlot.getInt(i));
        }
        return false;
    }

    @Override
    public boolean isFull() {
        for (IFluidHandler handler : mHandlers) {
            if (!handler.isFull()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).isEmpty(mSelectSlot.getInt(i));
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        for (IFluidHandler handler : mHandlers) {
            if (!handler.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getCapacity(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).getCapacity(mSelectSlot.getInt(i));
        }
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        FluidStack stack = resource.copy();
        for (IFluidHandler handler : mHandlers) {
            stack.reduce(handler.drain(resource, doDrain).getAmount());
        }
        return new FluidStack(resource.getFluid(), resource.getAmount() - stack.getAmount());
    }

    @Override
    public FluidStack drain(int slot, FluidStack resource, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).drain(mSelectSlot.getInt(i), resource, doDrain);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).drain(mSelectSlot.getInt(i), maxDrain, doDrain);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack stack = FluidStack.EMPTY;
        for (IFluidHandler handler : mHandlers) {
            if (stack.isEmpty()) {
                FluidStack drain = handler.drain(maxDrain, doDrain);
                if (!drain.isEmpty()) {
                    stack = new FluidStack(drain.getFluid(), maxDrain - stack.getAmount());
                }
            } else {
                stack.reduce(handler.drain(stack, doDrain).getAmount());
                if (stack.isEmpty()) {
                    return new FluidStack(stack.getFluid(), maxDrain);
                }
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT compound) {
        return compound;
    }

    @Override
    public FluidStack drainNotFilter(int slot, Fluid fluid, int amount, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).drainNotFilter(mSelectSlot.getInt(i), fluid, amount, doDrain);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public void setNotFilter(int slot, Fluid fluid, int amount) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).setNotFilter(mSelectSlot.getInt(i), fluid, amount);
        }
    }

    @Override
    public int fillNotFilter(int slot, Fluid fluid, int amount, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).fillNotFilter(mSelectSlot.getInt(i), fluid, amount, doDrain);
        }
        return 0;
    }

    public Readonly readonly() {
        return mReadonly;
    }

    @Override
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) { }

    private class Readonly implements IFluidHandler {

        @Override
        public FluidTankInfo[] getTankInfos() {
            return new FluidTankInfo[0];
        }

        @Override
        public Fluid getFluid(int slot) {
            return TankHandlerVisitor.this.getFluid(slot);
        }

        @Override
        public FluidStack getFluidStack(int slot) {
            return TankHandlerVisitor.this.getFluidStack(slot);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public void setFluid(int slot, Fluid fluid, int amount) { }

        @Override
        public int getSize() {
            return TankHandlerVisitor.this.getSize();
        }

        @Override
        public boolean isFull(int slot) {
            return TankHandlerVisitor.this.isFull(slot);
        }

        @Override
        public boolean isFull() {
            return TankHandlerVisitor.this.isFull();
        }

        @Override
        public boolean isEmpty(int slot) {
            return TankHandlerVisitor.this.isEmpty(slot);
        }

        @Override
        public boolean isEmpty() {
            return TankHandlerVisitor.this.isEmpty();
        }

        @Override
        public int getCapacity(int slot) {
            return TankHandlerVisitor.this.getCapacity(slot);
        }

        @Override
        public int fill(int slot, FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int slot, FluidStack resource, boolean doDrain) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public CompoundNBT write(@Nonnull CompoundNBT compound) {
            return compound;
        }

        @Override
        public FluidStack drainNotFilter(int slot, Fluid fluid, int amount, boolean doDrain) {
            return FluidStack.EMPTY;
        }

        @Override
        public void setNotFilter(int slot, Fluid fluid, int amount) { }

        @Override
        public int fillNotFilter(int slot, Fluid fluid, int amount, boolean doDrain) {
            return 0;
        }

        @Override
        public CompoundNBT serializeNBT() {
            return new CompoundNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) { }
    }
}
