package com.elementtimes.elementcore.api.template.capability;

import com.elementtimes.elementcore.api.interfaces.function.IILPredicate;
import com.elementtimes.elementcore.api.utils.CollectUtils;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * 自定义实现的 IFluidHandler
 * 实现了 NBT 序列化/反序列化
 * 实现了输入输出的过滤功能
 *
 * @author luqin2007
 */
public class ProxyTankHandler implements IFluidHandler, INBTSerializable<CompoundNBT> {

    protected Tank[] mTanks;
    protected IILPredicate.FluidPredicate mFillValid, mDrainValid;
    protected IntSet mFillSlots = new IntOpenHashSet();
    protected IntSet mDrainSlots = new IntOpenHashSet();
    protected long ioChangeTime = 0;
    public Proxy empty;

    public ProxyTankHandler(int size, int capability, IILPredicate.FluidPredicate isFillValid, IILPredicate.FluidPredicate isDrainValid) {
        mTanks = new Tank[size];
        mFillValid = isFillValid;
        mDrainValid = isDrainValid;
        for (int i = 0; i < mTanks.length; i++) {
            mTanks[i] = new Tank(i, capability);
        }
        setCanFill(0, size, true);
        setCanDrain(0, size, true);
        empty = new Proxy();
    }

    public void setCanFill(boolean canFill, int... slots) {
        setIO(mFillSlots, canFill, slots);
    }

    public void setCanFill(int fromInclusive, int toExclusive, boolean canFill) {
        setIO(mFillSlots, canFill, CollectUtils.rangeArr(fromInclusive, toExclusive));
    }

    public void setCanDrain(boolean canDrain, int... slots) {
        setIO(mDrainSlots, canDrain, slots);
    }

    public void setCanDrain(int fromInclusive, int toExclusive, boolean canDrain) {
        setIO(mDrainSlots, canDrain, CollectUtils.rangeArr(fromInclusive, toExclusive));
    }

    @SuppressWarnings("DuplicatedCode")
    protected void setIO(IntSet ioSet, boolean add, int[] slots) {
        boolean isChanged = false;
        if (add) {
            for (int slot : slots) {
                isChanged = ioSet.add(slot) | isChanged;
            }
        } else {
            for (int slot : slots) {
                isChanged = ioSet.remove(slot) | isChanged;
            }
        }
        if (isChanged) {
            ioChangeTime = System.currentTimeMillis();
        }
    }

    public FluidStack setFluid(int slot, FluidStack stack) {
        Tank tank = mTanks[slot];
        FluidStack fluid = tank.getFluid();
        tank.setFluid(stack);
        return fluid;
    }

    public FluidStack setFluidIgnoreCheck(int slot, FluidStack stack) {
        Tank tank = mTanks[slot];
        FluidStack fluid = tank.getFluid();
        tank.setFluidIgnoreCheck(stack);
        return fluid;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        int filled = 0;
        FluidStack stack = fluidStack.copy();
        for (Tank tank : mTanks) {
            int f = tank.fill(fluidStack, fluidAction);
            stack.shrink(f);
            filled += f;
            if (stack.isEmpty()) {
                break;
            }
        }
        return filled;
    }

    public int fill(int slot, FluidStack fluidStack, FluidAction fluidAction) {
        return mTanks[slot].fill(fluidStack, fluidAction);
    }

    public int fillIgnoreCheck(int slot, FluidStack resource, FluidAction action) {
        return mTanks[slot].fillIgnoreCheck(resource, action);
    }

    @Nonnull
    public FluidStack drain(int slot, FluidStack fluidStack, FluidAction fluidAction) {
        return mTanks[slot].drain(fluidStack, fluidAction);
    }

    @Nonnull
    public FluidStack drain(int slot, int maxDrain, FluidAction fluidAction) {
        return mTanks[slot].drain(maxDrain, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        int drained = 0;
        FluidStack stack = fluidStack.copy();
        for (Tank tank : mTanks) {
            int d = tank.drain(stack, fluidAction).getAmount();
            drained += d;
            stack.shrink(d);
            if (stack.isEmpty()) {
                break;
            }
        }
        return drained == 0 ? FluidStack.EMPTY : new FluidStack(fluidStack, drained);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction fluidAction) {
        FluidStack drained = FluidStack.EMPTY;
        for (Tank tank : mTanks) {
            if (drained.isEmpty()) {
                drained = tank.drain(maxDrain, fluidAction);
            } else {
                drained.grow(tank.drain(maxDrain - drained.getAmount(), fluidAction).getAmount());
                if (drained.getAmount() == maxDrain) {
                    break;
                }
            }
        }
        return drained;
    }

    @Nonnull
    public FluidStack drainIgnoreCheck(int slot, FluidStack resource, FluidAction action) {
        return mTanks[slot].drainIgnoreCheck(resource, action);
    }

    @Nonnull
    public FluidStack drainIgnoreCheck(int slot, int maxDrain, FluidAction action) {
        return mTanks[slot].drainIgnoreCheck(maxDrain, action);
    }

    @Override
    public int getTanks() {
        return mTanks.length;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int slot) {
        return mTanks[slot].getFluid();
    }

    @Override
    public int getTankCapacity(int slot) {
        return mTanks[slot].getCapacity();
    }

    @Override
    public boolean isFluidValid(int slot, @Nonnull FluidStack fluidStack) {
        return mTanks[slot].isFluidValid(fluidStack);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT list = new ListNBT();
        for (Tank tank : mTanks) {
            list.add(tank.serializeNBT());
        }
        nbt.put("f", list);
        nbt.putIntArray("d", mDrainSlots.toIntArray());
        nbt.putIntArray("f", mFillSlots.toIntArray());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ListNBT list = nbt.getList("f", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < mTanks.length; i++) {
            mTanks[i].deserializeNBT(list.getCompound(i));
        }
        mDrainSlots.clear();
        int[] ds = nbt.getIntArray("d");
        for (int d : ds) {
            mDrainSlots.add(d);
        }
        mFillSlots.clear();
        int[] fs = nbt.getIntArray("f");
        for (int f : fs) {
            mFillSlots.add(f);
        }
        ioChangeTime = System.currentTimeMillis();
    }

    class Tank extends FluidTank implements INBTSerializable<CompoundNBT> {

        protected boolean noCheck = false;
        protected int slot;
        protected boolean isFill = false;

        public Tank(int slot, int capacity) {
            super(capacity);
            this.validator = f -> noCheck || (isFill ? mFillValid.test(slot, f.getAmount(), f) : mDrainValid.test(slot, f.getAmount(), f));
            this.slot = slot;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            isFill = false;
            if (mDrainSlots.contains(slot) && mDrainValid.test(slot, maxDrain, FluidStack.EMPTY)) {
                return super.drain(maxDrain, action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            isFill = false;
            if (mDrainSlots.contains(slot) && mDrainValid.test(slot, resource.getAmount(), resource)) {
                return super.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        public FluidStack drainIgnoreCheck(int maxDrain, FluidAction action) {
            noCheck = true;
            FluidStack drain = this.drain(maxDrain, action);
            noCheck = false;
            return drain;
        }

        @Nonnull
        public FluidStack drainIgnoreCheck(FluidStack resource, FluidAction action) {
            noCheck = true;
            FluidStack drain = this.drain(resource, action);
            noCheck = false;
            return drain;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            isFill = true;
            if (mFillSlots.contains(slot)) {
                return super.fill(resource, action);
            }
            return 0;
        }

        public int fillIgnoreCheck(FluidStack resource, FluidAction action) {
            noCheck = true;
            isFill = true;
            int fill = this.fill(resource, action);
            noCheck = false;
            return fill;
        }

        @Override
        public void setFluid(FluidStack stack) {
            if (isFluidValid(stack)) {
                setFluidIgnoreCheck(stack);
            }
        }

        public void setFluidIgnoreCheck(FluidStack stack) {
            super.setFluid(stack);
        }

        @Override
        public Tank setCapacity(int capacity) {
            this.capacity = capacity;
            FluidStack fluid = getFluid();
            int amount = fluid.getAmount();
            if (amount > 0) {
                fluid.setAmount(Math.min(capacity, amount));
            }
            return this;
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("i", slot);
            nbt.putInt("c", getCapacity());
            nbt.putInt("a", fluid.getAmount());
            nbt.putString("n", fluid.getFluid().getRegistryName().toString());
            if (fluid.hasTag()) {
                nbt.put("t", fluid.getTag());
            }
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            slot = nbt.getInt("i");
            String fluidName = nbt.getString("n");
            Fluid value = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
            if (value == null || value == Fluids.EMPTY) {
                setFluidIgnoreCheck(FluidStack.EMPTY);
            } else {
                int amount = nbt.getInt("a");
                CompoundNBT n = nbt.contains("t") ? nbt.getCompound("t") : null;
                setFluidIgnoreCheck(new FluidStack(value, amount, n));
            }
            setCapacity(nbt.getInt("c"));
        }
    }

    public class Proxy implements IFluidHandler, INBTSerializable<CompoundNBT> {

        private final boolean canFill, canDrain;
        private final boolean isEmpty;
        private long captureTime = -1;
        private int[] handlerSlot;
        private int capability;

        public Proxy(boolean canFill, boolean canDrain) {
            this.canFill = canFill;
            this.canDrain = canDrain;
            isEmpty = false;
            reload();
        }

        private Proxy() {
            this.canFill = false;
            this.canDrain = false;
            isEmpty = true;

            handlerSlot = new int[0];
            capability = 0;
        }

        @SuppressWarnings("DuplicatedCode")
        protected void reload() {
            if (!isEmpty && captureTime < ioChangeTime) {
                captureTime = ioChangeTime;
                IntSet set = new IntArraySet();
                if (canFill) {
                    set.addAll(mFillSlots);
                }
                if (canDrain) {
                    set.addAll(mDrainSlots);
                }
                capability = set.size();
                handlerSlot = set.toIntArray();
                Arrays.sort(handlerSlot);
            }
        }

        protected boolean checkSlot(int slot) {
            reload();
            return slot < capability;
        }

        public int fill(int slot, FluidStack fluidStack, FluidAction fluidAction) {
            if (canFill && checkSlot(slot)) {
                return ProxyTankHandler.this.fill(handlerSlot[slot], fluidStack, fluidAction);
            }
            return 0;
        }

        @Override
        public int fill(FluidStack fluidStack, FluidAction fluidAction) {
            if (canFill) {
                return ProxyTankHandler.this.fill(fluidStack, fluidAction);
            }
            return 0;
        }

        public int fillIgnoreCheck(int slot, FluidStack resource, FluidAction action) {
            if (canFill && checkSlot(slot)) {
                return ProxyTankHandler.this.fill(handlerSlot[slot], resource, action);
            }
            return 0;
        }

        public int fillIgnoreCheck2(int slot, FluidStack resource, FluidAction action) {
            if (checkSlot(slot)) {
                return ProxyTankHandler.this.fill(handlerSlot[slot], resource, action);
            }
            return 0;
        }

        @Nonnull
        public FluidStack drain(int slot, FluidStack fluidStack, FluidAction fluidAction) {
            if (canDrain && checkSlot(slot)) {
                return ProxyTankHandler.this.drain(handlerSlot[slot], fluidStack, fluidAction);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        public FluidStack drain(int slot, int maxDrain, FluidAction fluidAction) {
            if (canDrain && checkSlot(slot)) {
                return ProxyTankHandler.this.drain(handlerSlot[slot], maxDrain, fluidAction);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
            if (canDrain) {
                return ProxyTankHandler.this.drain(fluidStack, fluidAction);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int i, FluidAction fluidAction) {
            if (canDrain && checkSlot(i)) {
                return ProxyTankHandler.this.drain(handlerSlot[i], fluidAction);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        public FluidStack drainIgnoreCheck(int slot, FluidStack resource, FluidAction action) {
            if (canDrain && checkSlot(slot)) {
                return ProxyTankHandler.this.drainIgnoreCheck(handlerSlot[slot], resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        public FluidStack drainIgnoreCheck2(int slot, FluidStack resource, FluidAction action) {
            if (checkSlot(slot)) {
                return ProxyTankHandler.this.drainIgnoreCheck(handlerSlot[slot], resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        public FluidStack drainIgnoreCheck(int slot, int maxDrain, FluidAction action) {
            if (canDrain && checkSlot(slot)) {
                return ProxyTankHandler.this.drainIgnoreCheck(handlerSlot[slot], maxDrain, action);
            }
            return FluidStack.EMPTY;
        }

        @Nonnull
        public FluidStack drainIgnoreCheck2(int slot, int maxDrain, FluidAction action) {
            if (checkSlot(slot)) {
                return ProxyTankHandler.this.drainIgnoreCheck(handlerSlot[slot], maxDrain, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public CompoundNBT serializeNBT() {
            return new CompoundNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) { }

        @Override
        public int getTanks() {
            return capability;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int i) {
            if (checkSlot(i)) {
                return ProxyTankHandler.this.getFluidInTank(handlerSlot[i]);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int i) {
            if (checkSlot(i)) {
                return ProxyTankHandler.this.getTankCapacity(handlerSlot[i]);
            }
            return 0;
        }

        @Override
        public boolean isFluidValid(int i, @Nonnull FluidStack fluidStack) {
            if (checkSlot(i)) {
                return ProxyTankHandler.this.isFluidValid(handlerSlot[i], fluidStack);
            }
            return false;
        }
    }
}
