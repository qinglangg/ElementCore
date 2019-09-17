package com.elementtimes.elementcore.api.template.capability.fluid;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 类似 Java 的 SubList，不过这个做的是加法，对多个 IFluidHandler 的访问器
 * @author luqin2007
 */
public class TankHandlerVisitor implements ITankHandler {

    private ITankHandler[] mHandlers;
    private int[] mTankSlots;
    private Readonly mReadonly;
    private int mSum;

    // 使用 list 兼容多人访问
    private IntArrayList mSelect;
    private IntArrayList mSelectSlot;
    private ArrayList<ITankHandler> mSelectHandler;

    public TankHandlerVisitor(ITankHandler... handlers) {
        mHandlers = handlers;
        mTankSlots = Arrays.stream(handlers).mapToInt(ITankHandler::size).toArray();
        mSum = Arrays.stream(mTankSlots).sum();
        mReadonly = new Readonly();
        mSelect = new IntArrayList(mSum);
        mSelectSlot = new IntArrayList(mSum);
        mSelectHandler = new ArrayList<>(mSum);
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
    public IFluidTankProperties[] getTankProperties() {
        int count = 0;
        final IFluidTankProperties[][] propertyArrays = new IFluidTankProperties[mHandlers.length][];
        for (int i = 0; i < mHandlers.length; i++) {
            propertyArrays[i] = mHandlers[i].getTankProperties();
            count += propertyArrays[i].length;
        }
        final IFluidTankProperties[] properties = new IFluidTankProperties[count];
        count = 0;
        for (IFluidTankProperties[] tankProperties : propertyArrays) {
            System.arraycopy(tankProperties, 0, properties, count, tankProperties.length);
            count += tankProperties.length;
        }
        return properties;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource != null) {
            FluidStack stack = resource.copy();
            for (IFluidHandler handler : mHandlers) {
                stack.amount = handler.fill(stack, doFill);
                if (stack.amount <= 0) {
                    return 0;
                }
            }
            return stack.amount;
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource != null) {
            FluidStack stack = resource.copy();
            for (IFluidHandler handler : mHandlers) {
                stack = handler.drain(stack, doDrain);
                if (stack == null) {
                    return null;
                }
            }
            return stack;
        }
        return null;
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
    public int fillIgnoreCheck(int slot, FluidStack resource, boolean doFill) {
        final int i = select(slot);
        if (i >= 0) {
            mSelectHandler.get(i).fillIgnoreCheck(mSelectSlot.getInt(i), resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(int slot, FluidStack resource, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).drain(mSelectSlot.getInt(i), resource, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drainIgnoreCheck(int slot, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).drain(mSelectSlot.getInt(i), maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drainIgnoreCheck(int slot, int maxDrain, boolean doDrain) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).drainIgnoreCheck(mSelectSlot.getInt(i), maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public int size() {
        return mSum;
    }

    @Override
    public FluidStack getFluid(int slot, boolean copy) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).getFluid(slot, copy);
        }
        return null;
    }

    @Override
    public int getCapacity(int slot) {
        final int i = select(slot);
        if (i >= 0) {
            return mSelectHandler.get(i).getCapacity(slot);
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack stack = null;
        for (IFluidHandler handler : mHandlers) {
            stack = handler.drain(maxDrain, doDrain);
            if (stack == null) {
                return null;
            }
        }
        return stack;
    }

    public Readonly readonly() {
        return mReadonly;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) { }

    private class Readonly implements ITankHandler {

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return TankHandlerVisitor.this.getTankProperties();
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public int fill(int slot, FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public int fillIgnoreCheck(int slot, FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(int slot, FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drainIgnoreCheck(int slot, FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(int slot, int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drainIgnoreCheck(int slot, int maxDrain, boolean doDrain) {
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {

        }

        @Override
        public int size() {
            return TankHandlerVisitor.this.mSum;
        }

        @Override
        public FluidStack getFluid(int slot, boolean copy) {
            return TankHandlerVisitor.this.getFluid(slot);
        }

        @Override
        public int getCapacity(int slot) {
            return TankHandlerVisitor.this.getCapacity(slot);
        }
    }
}
