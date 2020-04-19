package com.elementtimes.elementcore.api.template.capability;

import com.elementtimes.elementcore.api.interfaces.function.IILPredicate;
import com.elementtimes.elementcore.api.utils.CollectUtils;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.function.IntPredicate;

/**
 * 自定义实现的 IItemHandler
 * @author luqin2007
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ProxyItemHandler extends ItemStackHandler {

    protected IILPredicate.ItemPredicate mInsertValid = (a, b, c) -> true;
    protected IILPredicate.ItemPredicate mExtractValid = (a, b, c) -> true;
    protected IntSet mInsertSlots = new IntOpenHashSet();
    protected IntSet mExtractSlots = new IntOpenHashSet();
    protected long ioChangeTime = 0;
    protected boolean notCheck = false, isInsert = false;
    public Proxy empty;

    public ProxyItemHandler(int size) {
        super(size);
        setCanInsert(0, size, true);
        setCanExtract(0, size, true);
        empty = new Proxy();
    }

    @Override
    public void setSize(int size) {
        if (size < getSlots()) {
            boolean changed;
            changed = mInsertSlots.removeIf((IntPredicate) value -> value >= size);
            changed = mExtractSlots.removeIf((IntPredicate) value -> value >= size) | changed;
            if (changed) {
                ioChangeTime = System.currentTimeMillis();
            }
        }
        super.setSize(size);
    }

    public void setSlotIgnoreChangeListener(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        isInsert = true;
        return super.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int count, boolean simulate) {
        isInsert = false;
        ItemStack stack = getStackInSlot(slot).copy();
        stack.setCount(count);
        if (isItemValid(slot, stack, false)) {
            return super.extractItem(slot, count, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack insertItemIgnoreValid(int slot, @Nonnull ItemStack stack, boolean simulate) {
        notCheck = true;
        isInsert = true;
        ItemStack insertItem = this.insertItem(slot, stack, simulate);
        notCheck = false;
        return insertItem;
    }

    @Nonnull
    public ItemStack extractItemIgnoreValid(int slot, int amount, boolean simulate) {
        notCheck = true;
        isInsert = false;
        ItemStack extractItem = this.extractItem(slot, amount, simulate);
        notCheck = false;
        return extractItem;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isItemValid(slot, stack, isInsert);
    }

    protected boolean isItemValid(int slot, ItemStack stack, boolean insert) {
        if (notCheck) {
            return true;
        }
        if (insert) {
            return mInsertSlots.contains(slot) && mInsertValid.test(slot, stack.getCount(), stack);
        } else {
            return mExtractSlots.contains(slot) && mExtractValid.test(slot, stack.getCount(), stack);
        }
    }

    public void setCanInsert(boolean canInsert, int... slots) {
        setIO(mInsertSlots, canInsert, slots);
    }

    public void setCanInsert(int fromInclusive, int toExclusive, boolean canInsert) {
        setIO(mInsertSlots, canInsert, CollectUtils.rangeArr(fromInclusive, toExclusive));
    }

    public void setCanExtract(boolean canExtract, int... slots) {
        setIO(mExtractSlots, canExtract, slots);
    }

    public void setCanExtract(int fromInclusive, int toExclusive, boolean canExtract) {
        setIO(mExtractSlots, canExtract, CollectUtils.rangeArr(fromInclusive, toExclusive));
    }

    @SuppressWarnings("DuplicatedCode")
    protected void setIO(IntSet ioSet, boolean add, int[] slots) {
        boolean isChanged = false;
        int s = getSlots();
        if (add) {
            for (int slot : slots) {
                if (slot >= 0 && slot < s) {
                    isChanged = ioSet.add(slot) | isChanged;
                }
            }
        } else {
            for (int slot : slots) {
                if (slot >= 0 && slot < s) {
                    isChanged = ioSet.remove(slot) | isChanged;
                }
            }
        }
        if (isChanged) {
            ioChangeTime = System.currentTimeMillis();
        }
    }

    public void setExtractValid(IILPredicate.ItemPredicate extractValid) {
        mExtractValid = extractValid;
    }

    public IILPredicate.ItemPredicate getExtractValid() {
        return mExtractValid;
    }

    public void setInsertValid(IILPredicate.ItemPredicate insertValid) {
        mInsertValid = insertValid;
    }

    public IILPredicate.ItemPredicate getInsertValid() {
        return mInsertValid;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putIntArray("i", mInsertSlots.toIntArray());
        nbt.putIntArray("e", mExtractSlots.toIntArray());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        int[] is = nbt.getIntArray("i");
        mInsertSlots.clear();
        for (int i : is) {
            mInsertSlots.add(i);
        }
        int[] es = nbt.getIntArray("e");
        mExtractSlots.clear();
        for (int e : es) {
            mExtractSlots.add(e);
        }
        isInsert = false;
        notCheck = false;
        ioChangeTime = System.currentTimeMillis();
    }

    public class Proxy implements IItemHandler, IItemHandlerModifiable {

        private final boolean canInsert, canExtract;
        private final boolean isEmpty;
        private long captureTime = -1;
        private int[] handlerSlot;
        private int capability;

        public Proxy(boolean canInsert, boolean canExtract) {
            this.canInsert = canInsert;
            this.canExtract = canExtract;
            this.isEmpty = false;
            reload();
        }

        private Proxy() {
            canInsert = false;
            canExtract = false;
            isEmpty = true;

            handlerSlot = new int[0];
            capability = 0;
        }

        @SuppressWarnings("DuplicatedCode")
        protected void reload() {
            if (!isEmpty && captureTime < ioChangeTime) {
                captureTime = ioChangeTime;
                IntSet set = new IntArraySet();
                if (canInsert) {
                    set.addAll(mInsertSlots);
                }
                if (canExtract) {
                    set.addAll(mExtractSlots);
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

        @Override
        public int getSlots() {
            reload();
            return capability;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (checkSlot(slot)) {
                return ProxyItemHandler.this.getStackInSlot(handlerSlot[slot]);
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (canInsert && checkSlot(slot)) {
                return ProxyItemHandler.this.insertItem(handlerSlot[slot], stack, simulate);
            }
            return stack;
        }

        @Nonnull
        public ItemStack insertItemIgnoreValid(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (canInsert && checkSlot(slot)) {
                return ProxyItemHandler.this.insertItemIgnoreValid(handlerSlot[slot], stack, simulate);
            }
            return stack;
        }

        @Nonnull
        public ItemStack insertItemIgnoreValid2(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (checkSlot(slot)) {
                return ProxyItemHandler.this.insertItemIgnoreValid(handlerSlot[slot], stack, simulate);
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (canExtract && checkSlot(slot)) {
                return ProxyItemHandler.this.extractItem(handlerSlot[slot], amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        public ItemStack extractItemIgnoreValid(int slot, int amount, boolean simulate) {
            if (canExtract && checkSlot(slot)) {
                return ProxyItemHandler.this.extractItemIgnoreValid(handlerSlot[slot], amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        public ItemStack extractItemIgnoreValid2(int slot, int amount, boolean simulate) {
            if (checkSlot(slot)) {
                return ProxyItemHandler.this.extractItemIgnoreValid(handlerSlot[slot], amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            if (checkSlot(slot)) {
                return ProxyItemHandler.this.getSlotLimit(handlerSlot[slot]);
            }
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (checkSlot(slot)) {
                return ProxyItemHandler.this.isItemValid(handlerSlot[slot], stack);
            }
            return false;
        }

        public void setSlotIgnoreChangeListener(int slot, ItemStack stack) {
            if (checkSlot(slot)) {
                ProxyItemHandler.this.setSlotIgnoreChangeListener(handlerSlot[slot], stack);
            }
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            if (checkSlot(slot)) {
                ProxyItemHandler.this.setStackInSlot(handlerSlot[slot], stack);
            }
        }
    }

    public class ProxyIgnore implements IItemHandler, IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            ProxyItemHandler.this.setStackInSlot(slot, stack);
        }

        @Override
        public int getSlots() {
            return ProxyItemHandler.this.getSlots();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ProxyItemHandler.this.getStackInSlot(slot);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return ProxyItemHandler.this.insertItemIgnoreValid(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ProxyItemHandler.this.extractItemIgnoreValid(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return ProxyItemHandler.this.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }
}
